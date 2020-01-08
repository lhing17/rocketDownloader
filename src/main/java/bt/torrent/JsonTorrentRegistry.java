package bt.torrent;

import bt.data.Bitfield;
import bt.data.DataDescriptor;
import bt.data.IDataDescriptorFactory;
import bt.data.Storage;
import bt.event.EventSink;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentId;
import bt.runtime.Config;
import bt.service.IRuntimeLifecycleBinder;
import com.ccjiuhong.util.SerializeUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author G. Seinfeld
 * @since 2019/12/24
 */
@Slf4j
public class JsonTorrentRegistry implements TorrentRegistry, TorrentPersist {

    private IDataDescriptorFactory dataDescriptorFactory;
    private IRuntimeLifecycleBinder lifecycleBinder;
    private EventSink eventSink;

    private Set<TorrentId> torrentIds;
    private ConcurrentHashMap<TorrentId, Torrent> torrents;
    private ConcurrentHashMap<TorrentId, DefaultTorrentDescriptor> descriptors;
    private File torrentsJson;
    private File descriptorsJson;

    @Inject
    public JsonTorrentRegistry(IDataDescriptorFactory dataDescriptorFactory,
                               IRuntimeLifecycleBinder lifecycleBinder,
                               EventSink eventSink,
                               Config config) {
        File workingDirectory = new File(config.getWorkDirectory());
        if (!workingDirectory.exists()) {
            workingDirectory.mkdirs();
        }
        this.torrentsJson = new File(workingDirectory, "torrents.rd");
        this.descriptorsJson = new File(workingDirectory, "descriptors.rd");
        this.dataDescriptorFactory = dataDescriptorFactory;
        this.lifecycleBinder = lifecycleBinder;
        this.eventSink = eventSink;

        this.torrentIds = ConcurrentHashMap.newKeySet();
        this.torrents = new ConcurrentHashMap<>();
        this.descriptors = new ConcurrentHashMap<>();
    }

    public void serializeTorrents() {
        Map<String, Object> map = new ConcurrentHashMap<>();
        for (Map.Entry<TorrentId, Torrent> entry : torrents.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        SerializeUtil.serialize(map, torrentsJson);
    }

    public void serializeDescriptors() {
        Map<String, Object> map = new ConcurrentHashMap<>();
        for (Map.Entry<TorrentId, DefaultTorrentDescriptor> entry : descriptors.entrySet()) {
            Optional<BitSet> bitmask = Optional.ofNullable(entry.getValue())
                    .map(DefaultTorrentDescriptor::getDataDescriptor)
                    .map(DataDescriptor::getBitfield)
                    .map(Bitfield::getBitmask);
            bitmask.ifPresent(bitSet -> map.put(entry.getKey().toString(), bitSet));
        }
        SerializeUtil.serialize(map, descriptorsJson);
    }


    private ConcurrentHashMap<TorrentId, Torrent> deserializeTorrents(File torrentsJson) {
        try {
            return SerializeUtil.deserialize(torrentsJson, ConcurrentHashMap.class);
        } catch (Exception e) {
            log.warn("无法解析torrents.rd", e);
            return new ConcurrentHashMap<>();
        }
    }

    private ConcurrentHashMap<TorrentId, DefaultTorrentDescriptor> deserializeDescriptors(File descriptorsJson) {
        try {
            ConcurrentHashMap<TorrentId, BitSet> bitSetMap = SerializeUtil.deserialize(descriptorsJson, ConcurrentHashMap.class);
            for (TorrentId torrentId : bitSetMap.keySet()) {
                DefaultTorrentDescriptor descriptor = (DefaultTorrentDescriptor) register(torrentId);
            }
            return null;
        } catch (Exception e) {
            log.warn("无法解析descriptors.rd", e);
            return new ConcurrentHashMap<>();
        }
    }

    @Override
    public Collection<Torrent> getTorrents() {
        return Collections.unmodifiableCollection(torrents.values());
    }

    @Override
    public Collection<TorrentId> getTorrentIds() {
        return Collections.unmodifiableCollection(torrentIds);
    }

    @Override
    public Optional<Torrent> getTorrent(TorrentId torrentId) {
        Objects.requireNonNull(torrentId, "Missing torrent ID");
        return Optional.ofNullable(torrents.get(torrentId));
    }

    @Override
    public Optional<TorrentDescriptor> getDescriptor(Torrent torrent) {
        return Optional.ofNullable(descriptors.get(torrent.getTorrentId()));
    }


    @Override
    public Optional<TorrentDescriptor> getDescriptor(TorrentId torrentId) {
        Objects.requireNonNull(torrentId, "Missing torrent ID");
        return Optional.ofNullable(descriptors.get(torrentId));
    }

    @Override
    public TorrentDescriptor getOrCreateDescriptor(Torrent torrent, Storage storage) {
        return register(torrent, storage);
    }

    @Override
    public TorrentDescriptor register(Torrent torrent, Storage storage) {
        TorrentId torrentId = torrent.getTorrentId();

        DefaultTorrentDescriptor descriptor = descriptors.get(torrentId);
        if (descriptor != null) {
            if (descriptor.getDataDescriptor() == null) {
                descriptor.setDataDescriptor(dataDescriptorFactory.createDescriptor(torrent, storage));
            }
        } else {
            descriptor = getDefaultTorrentDescriptor(torrentId);
        }

        torrents.putIfAbsent(torrentId, torrent);
        return descriptor;
    }

    @Override
    public TorrentDescriptor register(TorrentId torrentId) {
        return getDescriptor(torrentId).orElseGet(() -> getDefaultTorrentDescriptor(torrentId));
    }

    @NotNull
    private DefaultTorrentDescriptor getDefaultTorrentDescriptor(TorrentId torrentId) {
        DefaultTorrentDescriptor descriptor = new DefaultTorrentDescriptor(torrentId, eventSink);

        DefaultTorrentDescriptor existing = descriptors.putIfAbsent(torrentId, descriptor);
        if (existing != null) {
            descriptor = existing;
        } else {
            torrentIds.add(torrentId);
            addShutdownHook(torrentId, descriptor);
        }
        return descriptor;
    }

    @Override
    public boolean isSupportedAndActive(TorrentId torrentId) {
        Optional<TorrentDescriptor> descriptor = getDescriptor(torrentId);
        // it's OK if descriptor is not present -- torrent might be being fetched at the time
        return getTorrentIds().contains(torrentId)
                && (descriptor.isEmpty() || descriptor.get().isActive());
    }

    private void addShutdownHook(TorrentId torrentId, TorrentDescriptor descriptor) {
        lifecycleBinder.onShutdown("Closing data descriptor for torrent ID: " + torrentId, () -> {
            if (descriptor.getDataDescriptor() != null) {
                try {
                    descriptor.getDataDescriptor().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
