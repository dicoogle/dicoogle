import Reflux from "reflux";
import * as UserActions from "../actions/userActions";
import { Endpoints } from "../constants/endpoints";
import dicoogleClient from "dicoogle-client";

const UserStore = Reflux.createStore({
  listenables: UserActions,
  init: function() {
    this._contents = {};

    this._isLoggedIn = false;
    this._username = "";
    this._isAdmin = false;
    this._roles = [];
    this._token = "";
    this.dicoogle = dicoogleClient(Endpoints.base);
  },

  saveLocalStore: function() {
    localStorage.setItem(
      "user",
      JSON.stringify({
        isAdmin: this._isAdmin,
        username: this._username,
        roles: this._roles,
        token: this._token
      })
    );
  },
  loadLocalStore(user) {
    if (!user) {
      user = localStorage.getItem("user");
    }
    if (user) {
      console.log("Loading previous session from local store");
      let userData = JSON.parse(user);
      this._isAdmin = userData.isAdmin;
      this._username = userData.username;
      this._roles = userData.roles;
      this._token = userData.token;
      this.dicoogle.setToken(this._token);
      localStorage.setItem("token", this._token);
      // assume that session is still alive
      this._isLoggedIn = true;
      console.log(
        `> ${this._username} (admin: ${this._isAdmin}) | token: ${this._token}`
      );
    }

    if (!this.dicoogle.isAuthenticated()) {
      this.trigger({
        isLoggedIn: false,
      });
      return;
    }

    // check if session is still alive
    this.dicoogle.request("GET", "login").end((error, data) => {
      this._loginFailed = false;
      if (error) {
        this._isLoggedIn = false;
        this._loginFailed = true;
        localStorage.removeItem("token");
      }

      const o = {
        loginFailed: this._loginFailed,
        isLoggedIn: this._isLoggedIn,
        username: this._username,
        roles: this._roles,
        isAdmin: this._isAdmin,
        token: this._token,
        success: true
      };

      this.trigger(o);
    });
  },
  onLogin: function(user, pass) {
    console.log("onLogin");

    this.dicoogle.login(user, pass, (error, data) => {
      if (error) {
        this.trigger({
          success: false,
          loginFailed: true
        });
        return;
      }
      this._username = data.user;
      this._isAdmin = data.admin;
      this._token = data.token;
      this._roles = data.roles;
      this._isLoggedIn = true;
      localStorage.token = this._token;
      console.log("Saving token to local storage:", localStorage.token);
      this.saveLocalStore();
      this.trigger({
        isLoggedIn: true,
        success: true,
        username: this._username,
        roles: this._roles,
        isAdmin: this._isAdmin,
        token: this._token
      });
    });
  },

  onLogout() {
    this.dicoogle.logout(err => {
      if (err) {
        console.error(err);
      }
    });
    this._isLoggedIn = false;
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  getUsername: function() {
    return this._username;
  },
  isAdmin: function() {
    return this._isAdmin;
  },
  getLogginState: function() {
    return this._isLoggedIn;
  }
});

export default UserStore;
