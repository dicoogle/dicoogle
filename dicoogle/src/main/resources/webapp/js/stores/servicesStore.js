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

  async onGetStorage() {
    try {
      let data = await this.dicoogle.storage.getStatus();
      this._contents.storageRunning = data.isRunning;
      this._contents.storagePort = data.port;
      this._contents.storageHostname = data.hostname;
      this._contents.storageAutostart = data.autostart;
      this.trigger(this._contents);
    } catch (error) {
      console.error("onGetStorage: failure", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onGetQuery() {
    try {
      let data = await this.dicoogle.queryRetrieve.getStatus();
      this._contents.queryRunning = data.isRunning;
      this._contents.queryPort = data.port;
      this._contents.queryHostname = data.hostname;
      this._contents.queryAutostart = data.autostart;
      this.trigger(this._contents);
    } catch (error) {
      console.error("onGetQuery: failure");
      this.trigger({ error: "Dicoogle service error" });
      return;
    }
  },

  async onSetStorage(running) {
    try {
      if (running) {
        await this.dicoogle.storage.start();
      } else {
        await this.dicoogle.storage.stop();
      }
      this._contents.storageRunning = running;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetStorageAutostart(enabled) {
    try {
      await this.dicoogle.storage.configure({ autostart: enabled });
      this._contents.storageAutostart = enabled;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetStoragePort(port) {
    try {
      await this.dicoogle.storage.configure({ port });
      this._contents.storagePort = port;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetStorageHostname(hostname) {
    try {
      await this.dicoogle.storage.configure({ hostname })
      this._contents.storageHostname = hostname;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetQuery(running) {
    try {
      if (running) {
        await this.dicoogle.queryRetrieve.start();
      } else {
        await this.dicoogle.queryRetrieve.stop();
      }
      this._contents.queryRunning = running;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetQueryAutostart(enabled) {
    try {
      await this.dicoogle.queryRetrieve.configure({ autostart: enabled });
      this._contents.queryAutostart = enabled;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetQueryPort(port) {
    try {
      await this.dicoogle.queryRetrieve.configure({ port });
      this._contents.queryPort = port;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSetQueryHostname(hostname) {
    try {
      await this.dicoogle.queryRetrieve.configure({ hostname });
      this._contents.queryHostname = hostname;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onGetQuerySettings() {
    try {
      let data = await this.dicoogle.queryRetrieve.getDicomQuerySettings();
      this._querySettings = data;
      this._contents.querySettings = this._querySettings;
      this.trigger(this._contents);
    } catch (error) {
      console.error("Dicoogle service error", error);
      this.trigger({ error: "Dicoogle service error" });
    }
  },

  async onSaveQuerySettings(
    connectionTimeout,
    acceptTimeout,
    idleTimeout,
    maxAssociations,
    maxPduReceive,
    maxPduSend,
    responseTimeout
  ) {
    try {
      await this.dicoogle.queryRetrieve.setDicomQuerySettings({
          connectionTimeout,
          acceptTimeout,
          idleTimeout,
          maxAssociations,
          maxPduReceive,
          maxPduSend,
          responseTimeout
        });
    } catch (error) {
      this.trigger({ error: "Dicoogle service error" });
      console.error("Dicoogle service error", error);
    }
  }
});

export default ServicesStore;
