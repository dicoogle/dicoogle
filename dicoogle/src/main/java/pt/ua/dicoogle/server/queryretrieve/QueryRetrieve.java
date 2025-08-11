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
package pt.ua.dicoogle.server.queryretrieve;


import java.io.File;
import java.util.Collection;
import java.util.concurrent.Executor;

import pt.ua.dicoogle.core.settings.ServerSettingsManager;

import org.dcm4che2.data.UID;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.VerificationService;
import org.slf4j.LoggerFactory;

import pt.ua.dicoogle.utils.Platform;
import pt.ua.dicoogle.sdk.settings.server.ServerSettings;
import pt.ua.dicoogle.server.DicomNetwork;

/**
 *
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 */
public class QueryRetrieve extends DicomNetwork {

    /**** Class Atributes ****/


    ServerSettings.DicomServices.QueryRetrieve s =
            ServerSettingsManager.getSettings().getDicomServicesSettings().getQueryRetrieveSettings();

    /* Implemented SOP Classes */
    private Collection<String> sopClass = null;

    private EchoReplyService verifService = null;

    /* DEFAULT Implemented module name */
    private static final String MODULE_NAME = "DICOOGLE-STORAGE";
    /* Remote Application Entity (client) */
    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    /* Remote connection associated with remoteAE */
    private final NetworkConnection remoteConn = new NetworkConnection();
    /* Module device */
    private final Device device = new Device(MODULE_NAME);
    /* Local Application Entity (this server) 'ea' */
    private final NetworkApplicationEntity localAE = new NetworkApplicationEntity();
    /* Local connection associated with localAE 'conn' */
    private final NetworkConnection localConn = new NetworkConnection();
    /* True if server is allready started */
    private boolean started = false;
    /* True if server is allready started as a Windows Service*/
    private boolean startedAsService = false;
    /* Response (to clients) delay (in milisec) */
    private int rspdelay = 0;


    /* Module executor  -  Server thread */
    private static Executor executor = new NewThreadExecutor(MODULE_NAME);

    private Collection<String> transfCap = s.getTransferCapabilities();
    private TransferCapability[] tc = new TransferCapability[5];

    private static String[] multiSop = {UID.StudyRootQueryRetrieveInformationModelFIND,

            UID.PatientRootQueryRetrieveInformationModelFIND};

    private static String[] moveSop =
            {UID.StudyRootQueryRetrieveInformationModelMOVE, UID.PatientRootQueryRetrieveInformationModelMOVE};

    public QueryRetrieve() {

        super("DICOOGLE-QUERYRETRIEVE");

        // super(multiSop, executor);
        this.sopClass = s.getSOPClass();


        // DebugManager.getSettings().debug("SOP Class: ");
        // DebugManager.getSettings().debug(s.getSOPClasses());

        for (String s : transfCap) {
            // DebugManager.getSettings().debug("TransCap : " + s );
        }

        String[] arr = new String[transfCap.size()];
        transfCap.toArray(arr);

        tc[0] = new TransferCapability(UID.StudyRootQueryRetrieveInformationModelFIND, arr, TransferCapability.SCP);
        tc[1] = new TransferCapability(UID.StudyRootQueryRetrieveInformationModelMOVE, arr, TransferCapability.SCP);

        tc[2] = new TransferCapability(UID.PatientRootQueryRetrieveInformationModelFIND, arr, TransferCapability.SCP);
        tc[3] = new TransferCapability(UID.PatientRootQueryRetrieveInformationModelMOVE, arr, TransferCapability.SCP);
        String[] Verification = {UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian};
        tc[4] = new TransferCapability(UID.VerificationSOPClass, Verification, TransferCapability.SCP);

        this.verifService = null;
        /* server */
        this.started = false;
        this.startedAsService = false;
        this.rspdelay = s.getRspDelay();
        this.localAE.setInstalled(true);
        this.localAE.setAssociationAcceptor(true);
        this.localAE.setAssociationInitiator(false);
        this.localAE.setNetworkConnection(this.localConn);
        this.localAE.setAETitle(ServerSettingsManager.getSettings().getDicomServicesSettings().getAETitle());
        this.localAE.setTransferCapability(tc);
        this.localAE.setDimseRspTimeout(s.getDIMSERspTimeout());
        this.localAE.setIdleTimeout(s.getIdleTimeout());
        this.localAE.setMaxPDULengthReceive(s.getMaxPDULengthReceive() + 1000);
        this.localAE.setMaxPDULengthSend(s.getMaxPDULengthSend() + 1000);

        this.localAE.register(new CMoveServiceSCP(moveSop, executor));
        this.localAE.register(new CFindServiceSCP(multiSop, executor));
        this.localAE.register(new VerificationService());

        this.localConn.setPort(s.getPort());
        this.localConn.setHostname(s.getHostname());
        this.localConn.setMaxScpAssociations(s.getMaxClientAssoc());
        this.localConn.setAcceptTimeout(s.getAcceptTimeout());
        this.localConn.setConnectTimeout(s.getConnectionTimeout());

        this.device
                .setDescription(ServerSettingsManager.getSettings().getDicomServicesSettings().getDeviceDescription());
        this.device.setNetworkApplicationEntity(this.localAE);
        this.device.setNetworkConnection(this.localConn);
    }



    @Override
    public boolean doStartService() {


        if (this.device != null) {
            this.verifService = new EchoReplyService();
            CommandUtils.setIncludeUIDinRSP(true);

            try {
                this.device.startListening(QueryRetrieve.executor);
                this.verifService.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            this.started = true;
            // DebugManager.getSettings().debug("Starting server " +
            // "- cmove server was started right now .. ");
            this.startedAsService = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean doStopService() {
        this.device.stopListening();
        return true;
    }

}
