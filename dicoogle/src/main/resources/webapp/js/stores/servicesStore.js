import Reflux from "reflux";
import ServiceAction from "../actions/servicesAction";

import dicoogleClient from "dicoogle-client";

const ServicesStore = Reflux.createStore({
  listenables: ServiceAction,
  init: function() {
    this._storageRunning = false;
    this._storagePort = 0;
    this._storageHostname = '';
    this._storageAutostart = false;
    this._queryRunning = false;
    this._queryPort = 0;
    this._queryHostname = '';
    this._queryAutostart = false;

    this._querySettings = {
      acceptTimeout: "...",
      connectionTimeout: "...",
      idleTimeout: "...",
      maxAssociations: "...",
      maxPduReceive: "...",
      maxPduSend: "...",
      responseTimeout: "..."
    };

    this._contents = {
      storageRunning: false,
      storagePort: 0,
      storageHostname: '',
      storageAutostart: false,
      queryRunning: false,
      queryPort: 0,
      queryHostname: '',
      queryAutostart: false,
      querySettings: this._querySettings
    };

    this.dicoogle = dicoogleClient();
  },

  onGetStorage: function() {
    this.dicoogle.storage.getStatus((error, data) => {
      if (error) {
        console.error("onGetStorage: failure", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.storageRunning = data.isRunning;
      this._contents.storagePort = data.port;
      this._contents.storageHostname = data.hostname;
      this._contents.storageAutostart = data.autostart;
      this.trigger(this._contents);
    });
  },

  onGetQuery: function() {
    this.dicoogle.queryRetrieve.getStatus((error, data) => {
      if (error) {
        console.error("onGetQuery: failure");
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.queryRunning = data.isRunning;
      this._contents.queryPort = data.port;
      this._contents.queryHostname = data.hostname;
      this._contents.queryAutostart = data.autostart;
      this.trigger(this._contents);
    });
  },

  onSetStorage(running) {
    const callback = error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.storageRunning = running;
      this.trigger(this._contents);
    };

    if (running) {
      this.dicoogle.storage.start(callback);
    } else {
      this.dicoogle.storage.stop(callback);
    }
  },

  onSetStorageAutostart(enabled) {
    this.dicoogle.storage.configure({ autostart: enabled }, error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.storageAutostart = enabled;
      this.trigger(this._contents);
    });
  },

  onSetStoragePort(port) {
    this.dicoogle.storage.configure({ port }, error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.storagePort = port;
      this.trigger(this._contents);
    });
  },

  onSetStorageHostname(hostname) {
    // using generic request to set hostname
    // (not yet supported by dicoogle-client)
    this.dicoogle.request('POST', this.dicoogle.Endpoints.STORAGE_SERVICE)
      .query({ hostname })
      .end((error, _response) => {
        if (error) {
          console.error("Dicoogle service error", error);
          this.trigger({ error: "Dicoogle service error" });
          return;
        }

        this._contents.storageHostname = hostname;
        this.trigger(this._contents);
      });
  },

  onSetQuery(running) {
    const callback = error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.queryRunning = running;
      this.trigger(this._contents);
    };

    if (running) {
      this.dicoogle.queryRetrieve.start(callback);
    } else {
      this.dicoogle.queryRetrieve.stop(callback);
    }
  },

  onSetQueryAutostart(enabled) {
    this.dicoogle.queryRetrieve.configure({ autostart: enabled }, error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.queryAutostart = enabled;
      this.trigger(this._contents);
    });
  },

  onSetQueryPort(port) {
    this.dicoogle.queryRetrieve.configure({ port }, error => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._contents.queryPort = port;
      this.trigger(this._contents);
    });
  },

  onSetQueryHostname(hostname) {
    // using generic request to set hostname
    // (not yet supported by dicoogle-client)
    this.dicoogle.request('POST', this.dicoogle.Endpoints.QR_SERVICE)
      .query({ hostname })
      .end((error, _response) => {
        if (error) {
          console.error("Dicoogle service error", error);
          this.trigger({ error: "Dicoogle service error" });
          return;
        }

        this._contents.queryHostname = hostname;
        this.trigger(this._contents);
      });
  },

  onGetQuerySettings() {
    this.dicoogle.queryRetrieve.getDicomQuerySettings((error, data) => {
      if (error) {
        console.error("Dicoogle service error", error);
        this.trigger({ error: "Dicoogle service error" });
        return;
      }

      this._querySettings = data;
      this._contents.querySettings = this._querySettings;
      this.trigger(this._contents);
    });
  },

  onSaveQuerySettings(
    connectionTimeout,
    acceptTimeout,
    idleTimeout,
    maxAssociations,
    maxPduReceive,
    maxPduSend,
    responseTimeout
  ) {
    this.dicoogle.queryRetrieve.setDicomQuerySettings(
      {
        connectionTimeout,
        acceptTimeout,
        idleTimeout,
        maxAssociations,
        maxPduReceive,
        maxPduSend,
        responseTimeout
      },
      (error, data) => {
        if (error) {
          this.trigger({ error: "Dicoogle service error" });
          console.error("Dicoogle service error", error);
          return;
        }
      }
    );
  }
});

export default ServicesStore;
