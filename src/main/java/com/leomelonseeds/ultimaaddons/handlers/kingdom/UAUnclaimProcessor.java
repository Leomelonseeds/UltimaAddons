package com.leomelonseeds.ultimaaddons.handlers.kingdom;

import com.leomelonseeds.ultimaaddons.UltimaAddons;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.location.SimpleChunkLocation;
import org.kingdoms.constants.metadata.KingdomMetadata;
import org.kingdoms.constants.metadata.StandardKingdomMetadata;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.locale.KingdomsLang;
import org.kingdoms.locale.messenger.Messenger;
import org.kingdoms.managers.land.claiming.UnclaimProcessor;
import org.kingdoms.utils.chunks.ChunkConnections;

import java.util.HashSet;
import java.util.Set;

public class UAUnclaimProcessor extends UnclaimProcessor {

    protected UAUnclaimProcessor(SimpleChunkLocation chunk, KingdomPlayer kp, Kingdom kingdom) {
        super(chunk, kp, kingdom);
    }

    public static void register() {
        UnclaimProcessor.setBuilder(UAUnclaimProcessor::new);
    }

    @Override
    public Messenger disconnectsLandsAfterUnclaim() {
        SimpleChunkLocation set = getChunk();
        Kingdom kingdom = getKingdom();
        Set<SimpleChunkLocation> toCheck = new HashSet<>();
        Land cur = set.getLand();
        String curWorld = set.getWorld();
        KingdomMetadata outpostdata = cur.getMetadata().get(UltimaAddons.outpost_id);
        long outpostId = outpostdata == null ? 0 : ((StandardKingdomMetadata) outpostdata).getLong();
        kingdom.getLands().forEach(l -> {
            SimpleChunkLocation scl = l.getLocation();
            if (!scl.getWorld().equals(curWorld) || scl.equals(set)) {
                return;
            }

            KingdomMetadata ldata = l.getMetadata().get(UltimaAddons.outpost_id);
            if (ldata == null) {
                if (outpostId == 0) {
                    toCheck.add(scl);
                }
                return;
            }

            if (outpostId == ((StandardKingdomMetadata) ldata).getLong()) {
                toCheck.add(scl);
            }
        });

        if (ChunkConnections.getConnectedClusters(1, toCheck).size() > 1) {
            return KingdomsLang.COMMAND_UNCLAIM_DISCONNECTION;
        }

        return null;
    }
}
