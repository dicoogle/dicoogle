/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.server;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;


import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;


/**
 * DICOM Storage Service is provided by this class
 *
 * @author Luís Bastião Silva <bastiao@bmd-softwre.com>
 * @author Marco Pereira
 */

public class DicomStorage extends StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(DicomStorage.class);

    private SOPList list;
    private ServerSettings settings;

    private Executor executor = new LoggingExecutor(new NewThreadExecutor("DicoogleStorage"), LOG);
    private Device device = new Device("DicoogleStorage");
    private NetworkApplicationEntity nae = new NetworkApplicationEntity();
    private NetworkConnection nc = new NetworkConnection();

    private String path;

    private Set<String> alternativeAETs = new HashSet<>();
    private Set<String> priorityAETs = new HashSet<>();

    // Changed to support priority queue.
    private NetworkApplicationEntity[] naeArr = null;
    private AtomicLong seqNum = new AtomicLong(0L);

    /**
     *
     * @param Services List of supported SOP Classes
     * @param l list of Supported SOPClasses with supported Transfer Syntax
     */

    public DicomStorage(String[] Services, SOPList l) {
        // just because the call to super must be the first instruction
        super(Services);

        // our configuration format
        list = l;
        settings = ServerSettingsManager.getSettings();

        // Added default alternative AETitle.
        alternativeAETs.add(ServerSettingsManager.getSettings().getArchiveSettings().getNodeName());

        path = settings.getArchiveSettings().getMainDirectory();
        if (path == null) {
            path = "/dev/null";
        }

        this.priorityAETs = new HashSet<>(settings.getDicomServicesSettings().getPriorityAETitles());
        LoggerFactory.getLogger(DicomStorage.class).debug("Priority C-STORE: {}", this.priorityAETs);

        device.setNetworkApplicationEntity(nae);

        device.setNetworkConnection(nc);
        nae.setNetworkConnection(nc);

        // we accept assoociations, this is a server
        nae.setAssociationAcceptor(true);
        // we support the VerificationServiceSOP
        nae.register(new VerificationService());
        // and the StorageServiceSOP
        nae.register(this);

        nae.setAETitle(settings.getDicomServicesSettings().getAETitle());


        nc.setPort(settings.getDicomServicesSettings().getStorageSettings().getPort());
        nc.setHostname(settings.getDicomServicesSettings().getStorageSettings().getHostname());

        this.nae.setInstalled(true);
        this.nc.setMaxScpAssociations(Integer.parseInt(System.getProperty("dicoogle.cstore.maxScpAssociations", "50")));
        this.nc.setAcceptTimeout(Integer.parseInt(System.getProperty("dicoogle.cstore.acceptTimeout", "5000")));
        this.nc.setConnectTimeout(Integer.parseInt(System.getProperty("dicoogle.cstore.connectTimeout", "5000")));

        this.nae.setAssociationAcceptor(true);
        this.nae.setAssociationInitiator(false);

        int maxPDULengthReceive =
                settings.getDicomServicesSettings().getQueryRetrieveSettings().getMaxPDULengthReceive();
        int maxPDULengthSend = settings.getDicomServicesSettings().getQueryRetrieveSettings().getMaxPDULengthSend();

        this.nae.setDimseRspTimeout(
                Integer.parseInt(System.getProperty("dicoogle.cstore.dimseRspTimeout", "18000000")));
        this.nae.setIdleTimeout(Integer.parseInt(System.getProperty("dicoogle.cstore.idleTimeout", "18000000")));
        this.nae.setMaxPDULengthReceive(
                maxPDULengthReceive + Integer.parseInt(System.getProperty("dicoogle.cstore.appendMaxPDU", "1000")));
        this.nae.setMaxPDULengthSend(
                maxPDULengthSend + Integer.parseInt(System.getProperty("dicoogle.cstore.appendMaxPDU", "1000")));
        this.nae.setRetrieveRspTimeout(
                Integer.parseInt(System.getProperty("dicoogle.cstore.retrieveRspTimeout", "18000000")));


        // Added alternative AETitles.

        naeArr = new NetworkApplicationEntity[alternativeAETs.size() + 1];
        // Just adding the first AETitle
        naeArr[0] = nae;

        int k = 1;

        for (String alternativeAET : alternativeAETs) {
            NetworkApplicationEntity nae2 = new NetworkApplicationEntity();
            nae2.setNetworkConnection(nc);
            nae2.setDimseRspTimeout(
                    Integer.parseInt(System.getProperty("dicoogle.cstore.dimseRspTimeout", "18000000")));
            nae2.setIdleTimeout(Integer.parseInt(System.getProperty("dicoogle.cstore.idleTimeout", "18000000")));
            nae2.setMaxPDULengthReceive(
                    maxPDULengthReceive + Integer.parseInt(System.getProperty("dicoogle.cstore.appendMaxPDU", "1000")));
            nae2.setMaxPDULengthSend(
                    maxPDULengthSend + Integer.parseInt(System.getProperty("dicoogle.cstore.appendMaxPDU", "1000")));
            nae2.setRetrieveRspTimeout(
                    Integer.parseInt(System.getProperty("dicoogle.cstore.retrieveRspTimeout", "18000000")));

            // we accept assoociations, this is a server
            nae2.setAssociationAcceptor(true);
            // we support the VerificationServiceSOP
            nae2.register(new VerificationService());
            // and the StorageServiceSOP
            nae2.register(this);
            nae2.setAETitle(alternativeAET);
            ServerSettings settings = ServerSettingsManager.getSettings();
            Collection<String> caet = settings.getDicomServicesSettings().getAllowedAETitles();
            if (!caet.isEmpty()) {
                String[] array = new String[caet.size()];
                caet.toArray(array);
                nae2.setPreferredCallingAETitle(array);
            }
            naeArr[k] = nae2;
            k++;

        }

        // Just set the Network Application Entity array - which accepts a set of AEs.
        device.setNetworkApplicationEntity(naeArr);


        initTS(Services);
    }

    /**
     *  Sets the tranfer capability for this execution of the storage service
     *  @param Services Services to be supported
     */
    private void initTS(String[] Services) {
        int count = list.getAccepted();
        // System.out.println(count);
        TransferCapability[] tc = new TransferCapability[count + 1];
        String[] Verification = {UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian};
        String[] TS;
        TransfersStorage local;

        tc[0] = new TransferCapability(UID.VerificationSOPClass, Verification, TransferCapability.SCP);
        int j = 0;
        for (int i = 0; i < Services.length; i++) {
            count = 0;
            local = list.getTS(Services[i]);
            if (local.getAccepted()) {
                TS = local.getVerboseTS();
                if (TS != null) {

                    tc[j + 1] = new TransferCapability(Services[i], TS, TransferCapability.SCP);
                    j++;
                }
            }
        }

        // Setting the TS in all NetworkApplicationEntitys
        for (int i = 0; i < naeArr.length; i++) {

            naeArr[i].setTransferCapability(tc);
        }
        nae.setTransferCapability(tc);
    }

    @Override
    /**
     * Called when a C-Store Request has been accepted
     * Parameters defined by dcm4che2
     */
    public void cstore(final Association as, final int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid)
            throws DicomServiceException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking permission for {} to store {}", as.getCallingAET(), rq.getString(Tag.SOPInstanceUID));
        }

        boolean permited = false;
        Collection<String> allowedAETitles =
                ServerSettingsManager.getSettings().getDicomServicesSettings().getAllowedAETitles();
        if (allowedAETitles.isEmpty()) {
            permited = true;
        } else {
            permited = allowedAETitles.contains(as.getCallingAET());
        }

        if (!permited) {
            LOG.warn("Client association with {} NOT permitted!", as.getCallingAET());
            as.abort();

            return;
        }

        final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
        onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
        as.writeDimseRSP(pcid, rsp);
    }

    @Override
    /**
     * Actually do the job of saving received file on disk
     * on this server with extras such as Lucene indexing
     * and DICOMDIR update
     */
    protected void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid,
            DicomObject rsp) throws IOException, DicomServiceException {
        try {

            String cuid = rq.getString(Tag.AffectedSOPClassUID);
            String iuid = rq.getString(Tag.AffectedSOPInstanceUID);

            String sendingAet = as.getCallingAET();
            String receivingAet = as.getCalledAET();

            DicomObject d = dataStream.readDataset();

            d.initFileMetaInformation(cuid, iuid, tsuid);

            // constants are missing in dcm4che 2
            final int TAG_SENDING_APPLICATION_ENTITY_TITLE = 0x0002_0017;
            final int TAG_RECEIVING_APPLICATION_ENTITY_TITLE = 0x0002_0018;
            d.fileMetaInfo().putString(TAG_SENDING_APPLICATION_ENTITY_TITLE, VR.AE, sendingAet);
            d.fileMetaInfo().putString(TAG_RECEIVING_APPLICATION_ENTITY_TITLE, VR.AE, receivingAet);

            Iterable<StorageInterface> plugins = PluginController.getInstance().getStoragePlugins(true);

            for (StorageInterface storage : plugins) {
                URI uri = storage.store(d);
                if (uri != null) {
                    // enqueue to index
                    ImageElement element = new ImageElement(uri, as.getCallingAET(), seqNum.getAndIncrement());
                    IndexQueueWorker.getInstance().addElement(element);
                }
            }

        } catch (Exception e) {
            LOG.error("DICOM storage service failure:", e);
            throw new DicomServiceException(rq, Status.ProcessingFailure, e.getMessage());
        }
    }

    /**
     * A C-STORE entry.
     * For Each C-STORE RQ, an ImageElement is created
     * and put in the storage service's priority queue for indexing.
     * 
     * The priority criteria are, in descending order of importance:
     * 1. whether the calling AE title is in the list of priority AEs
     * 2. earliest sequence number
     *
     * This only happens after the store in Storage Plugins.
     */
    final class ImageElement implements Comparable<ImageElement> {
        private final URI uri;
        private final String callingAET;
        private final long seqNumber;

        ImageElement(URI uri, String callingAET, long seqNumber) {
            Objects.requireNonNull(uri);
            Objects.requireNonNull(callingAET);
            this.uri = uri;
            this.callingAET = callingAET;
            this.seqNumber = seqNumber;
        }

        public URI getUri() {
            return uri;
        }

        public String getCallingAET() {
            return callingAET;
        }

        public long getSequenceNumber() {
            return seqNumber;
        }

        @Override
        public int compareTo(ImageElement other) {
            return compareElementsImpl(DicomStorage.this.priorityAETs, this.callingAET, this.seqNumber,
                    other.callingAET, other.seqNumber);
        }
    }

    /** Standalone implementation of image element sorting criteria.
     * 
     * @param priorityAETs the set of calling AE titles
     * from which incoming images have a HIGHER priority
     * @param thisAETitle this image's AE title
     * @param thisSeqNumber this image's sequence number
     * @param otherAETitle the other image's AE title
     * @param otherSeqNumber the other image's sequence number
     * @return a number < 0 if this image has a higher priority than the other,
     * > 0 if the other image has a higher priority
     * (=0 should not happen because sequence numbers are unique)
     * @see {@link ImageElement#compareTo(ImageElement)}
     */
    static int compareElementsImpl(Set<String> priorityAETs, String thisAETitle, long thisSeqNumber,
            String otherAETitle, long otherSeqNumber) {
        boolean thisPriority = priorityAETs.contains(thisAETitle);
        boolean thatPriority = priorityAETs.contains(otherAETitle);

        int priorityOrder = Boolean.compare(thisPriority, thatPriority);

        if (priorityOrder != 0) {
            return -priorityOrder;
        }

        return Long.compare(thisSeqNumber, otherSeqNumber);
    }

    /*
     * Start the Storage Service
     * @throws java.io.IOException
     */
    public void start() throws IOException {
        device.startListening(executor);
        IndexQueueWorker.getInstance().start();
    }

    /**
     * Stop the storage service 
     */
    public void stop() {
        device.stopListening();
    }
}
