import React from "react";
import createReactClass from "create-react-class";
import * as PropTypes from "prop-types";
import * as UserActions from "../../actions/userActions";
import UserStore from "../../stores/userStore";

const LoginView = createReactClass({
  contextTypes: {
    router: PropTypes.object.isRequired
  },
  getInitialState: function() {
    return {
      data: {},
      status: "loading",
      failed: false,
      username: "",
      password: ""
    };
  },
  componentWillMount: function() {
    this.unsubscribe = UserStore.listen(this._onChange);
  },
  componentWillUnmount() {
    this.unsubscribe();
  },
  _onChange: function(data) {
    console.log(data);
    const { router } = this.context;
    if (data.loginFailed) {
      this.setState({ failed: true });
      return;
    }

    if (data.isLoggedIn && router.history.location.pathname === "/login") {
      router.history.replace("/search");
    }
  },

  render: function() {
    const guestCredentials = process.env.GUEST_USERNAME && [
      <hr key="0" />,
      <div key="1">
        Guest credentials: <br />
        <b>username:</b> {process.env.GUEST_USERNAME} <br />
        <b>password:</b> {process.env.GUEST_PASSWORD}
      </div>
    ];
    return (
      <div id="loginwrapper">
        <div className="loginbody">
          <section className="container row-fluid loginbox logincontainer">
            <img
              className="loginlogo"
              src="assets/logo.png"
              alt="Smiley face"
            />

            <div>
              <h4 style={{ textAlign: "center" }}>
                Improve your knowledge from your medical imaging repository.
              </h4>
            </div>

            <div className="loginA">
              <form className="form-horizontal">
                <p className="loginTextA">Sign In</p>
                <input
                  ref="user"
                  type="text"
                  id="username"
                  name="username"
                  placeholder="Username"
                  className="loginInputUsername form-control"
                  value={this.state.username}
                  onChange={this.handleUsernameChange}
                  onKeyDown={this.handleKeyDown}
                  autoComplete="username"
                />
                <input
                  ref="pass"
                  type="password"
                  id="password"
                  name="password"
                  placeholder="Password"
                  className="loginInputPassword form-control"
                  value={this.state.password}
                  onChange={this.handlePasswordChange}
                  onKeyDown={this.handleKeyDown}
                  autoComplete="current-password"
                />
                {this.state.failed && (
                  <p style={{ color: "red" }}>
                    {" "}
                    Login Failed. Please try again.{" "}
                  </p>
                )}
                <button
                  type="button"
                  className="btn submit btn_dicoogle"
                  onClick={this.onLoginClick}
                >
                  Login
                </button>
                {guestCredentials}
              </form>
            </div>
          </section>

          {/*to fill the empty space between the login and footer*/}
          <div id="filler" />

          <footer id="footer">
            <div style={{ width: "100%", textAlign: "center" }}>
              Community information and Learning Pack available on the{" "}
              <a target="_new" href="https://www.dicoogle.com">
                {" "}
                the Dicoogle website
              </a>.{" "}
              Commercial support, by
              <a target="_new" href="https://www.bmd-software.com">
                {" "}
                BMD Software
              </a>.
            </div>
            <div
              style={{ width: "100%", textAlign: "center" }}
              className="footercontainer"
            >
              <div style={{ display: "inline-block", width: "100%" }}>
                <a href="https://bioinformatics.ua.pt">
                  <img
                    src="assets/logos/logobio.png"
                    style={{ height: 40, margin: 5 }}
                  />
                </a>
                <a href="https://bmd-software.com/">
                  <img
                    src="assets/logos/logo.png"
                    style={{ height: 40, padding: 5, margin: 5 }}
                  />
                </a>
                <a href="https://www.ieeta.pt/">
                  <img
                    src="assets/logos/logo-ieeta.png"
                    style={{ height: 60, margin: 5 }}
                  />
                </a>
                <a href="https://www.ua.pt/">
                  <img
                    src="assets/logos/logo-ua.png"
                    style={{ height: 60, margin: 5 }}
                  />
                </a>
              </div>
              <div style={{ display: "inline-block" }}>
                <a>
                  <img
                    src="assets/logos/logoFCT_1.png"
                    style={{ height: 30, margin: 10 }}
                  />
                </a>
                <a>
                  <img
                    src="assets/logos/logoFCT_2.png"
                    style={{ height: 30, margin: 10 }}
                  />
                </a>
                <a>
                  <img
                    src="assets/logos/logoFCT_3.png"
                    style={{ height: 30, margin: 10 }}
                  />
                </a>
                <a>
                  <img
                    src="assets/logos/logoFCT_4.png"
                    style={{ height: 30, margin: 10 }}
                  />
                </a>
              </div>
            </div>
          </footer>
        </div>
      </div>
    );
  },

  handleKeyDown(e) {
    if (e.keyCode === 13) {
      this.onLoginClick();
    }
  },

  handleUsernameChange(e) {
    this.setState({ username: e.target.value });
  },

  handlePasswordChange(e) {
    this.setState({ password: e.target.value });
  },

  onLoginClick() {
    const { username, password } = this.state;
    UserActions.login(username, password);
  }
});

export default LoginView;
