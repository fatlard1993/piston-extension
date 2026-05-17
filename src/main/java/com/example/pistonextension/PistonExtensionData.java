package com.example.pistonextension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent saved data that stores per-piston extension lengths.
 * Keys are encoded BlockPos values (as longs), values are extension lengths (1-3).
 *
 * Uses the 26.1 Codec-based SavedDataType API for serialization.
 *
 * Note: Entries are cleaned up lazily. When a piston is destroyed, its
 * entry remains until the next time something queries or sets it.
 * This avoids the complexity of hooking into block removal events on
 * classes that may not declare the removal method directly.
 */
public class PistonExtensionData extends SavedData {

	private final Map<Long, Integer> extensionLengths;

	/**
	 * Codec entry for a single piston position + extension length pair.
	 */
	private record PistonEntry(long pos, int length) {
		public static final Codec<PistonEntry> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.LONG.fieldOf("pos").forGetter(PistonEntry::pos),
						Codec.INT.fieldOf("length").forGetter(PistonEntry::length)
				).apply(instance, PistonEntry::new)
		);
	}

	/**
	 * Codec for PistonExtensionData: a list of {pos, length} entries.
	 */
	public static final Codec<PistonExtensionData> CODEC = PistonEntry.CODEC.listOf()
			.fieldOf("pistons")
			.codec()
			.xmap(
					// Decode: list of entries -> PistonExtensionData
					entries -> {
						Map<Long, Integer> map = new HashMap<>();
						for (PistonEntry entry : entries) {
							if (entry.length() > 1) {
								map.put(entry.pos(), entry.length());
							}
						}
						return new PistonExtensionData(map);
					},
					// Encode: PistonExtensionData -> list of entries
					data -> data.extensionLengths.entrySet().stream()
							.map(e -> new PistonEntry(e.getKey(), e.getValue()))
							.toList()
			);

	public static final SavedDataType<PistonExtensionData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath("piston-extension", "data"),
			PistonExtensionData::new,
			CODEC,
			DataFixTypes.LEVEL
	);

	public PistonExtensionData() {
		this.extensionLengths = new HashMap<>();
	}

	public PistonExtensionData(Map<Long, Integer> extensionLengths) {
		this.extensionLengths = new HashMap<>(extensionLengths);
	}

	public int getExtensionLength(BlockPos pos) {
		return extensionLengths.getOrDefault(pos.asLong(), 1);
	}

	public void setExtensionLength(BlockPos pos, int length) {
		if (length <= 1) {
			extensionLengths.remove(pos.asLong());
		} else {
			extensionLengths.put(pos.asLong(), length);
		}
		setDirty();
	}

	public void removeEntry(BlockPos pos) {
		if (extensionLengths.remove(pos.asLong()) != null) {
			setDirty();
		}
	}

	public static PistonExtensionData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}
}
