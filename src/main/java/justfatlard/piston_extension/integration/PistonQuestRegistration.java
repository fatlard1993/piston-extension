package justfatlard.piston_extension.integration;

import java.util.Random;
import justfatlard.piston_extension.quest.LongPistonQuest;
import justfatlard.village_quests.api.QuestRegistry;
import justfatlard.village_quests.quest.VillagerQuest;
import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Offers the long-piston gate from the professions that build things.
 *
 * <p>Names village-quests types directly, so it must only be loaded behind the
 * isModLoaded guard in the entry point.
 */
public final class PistonQuestRegistration {
	private PistonQuestRegistration() {}

	private static final float OFFER_CHANCE = 0.10F;

	public static void register() {
		QuestRegistry.registerProfessionQuest("mason", PistonQuestRegistration::offer);
		QuestRegistry.registerProfessionQuest("toolsmith", PistonQuestRegistration::offer);
	}

	private static VillagerQuest offer(Villager villager, String villagerName, int reputation, Random random) {
		if (reputation < 15) return null;
		if (random.nextFloat() > OFFER_CHANCE) return null;

		// Anchored to the villager, so the gate gets built where they can see it
		// rather than wherever the player happened to be standing.
		return new LongPistonQuest(villagerName, villager.getUUID(), villager.blockPosition());
	}
}
