import React from "react";
import * as PropTypes from "prop-types";
import { FormGroup, FormControl } from "react-bootstrap";

export default class ServiceForm extends React.Component {
  static get propTypes() {
    return {
      caption: PropTypes.string.isRequired,
      running: PropTypes.bool.isRequired,
      dirtyPort: PropTypes.bool.isRequired, // port has unsaved changes
      onhold: PropTypes.bool,
      port: PropTypes.oneOfType([
        PropTypes.string.isRequired,
        PropTypes.number.isRequired
      ]).isRequired,
      hostname: PropTypes.string.isRequired,
      extraSettings: PropTypes.node,
      onStartService: PropTypes.func.isRequired,
      onStopService: PropTypes.func.isRequired,
      onChangePort: PropTypes.func.isRequired,
      onChangeHostname: PropTypes.func.isRequired,
      onToggleAutostart: PropTypes.func.isRequired,
      onSubmitPort: PropTypes.func.isRequired,
      onSubmitHostname: PropTypes.func.isRequired,
    };
  }

  constructor(props) {
    super(props);
    this.handlePortChange = this.handlePortChange.bind(this);
    this.handlePortKeyPress = this.handlePortKeyPress.bind(this);
    this.handleHostnameChange = this.handleHostnameChange.bind(this);
    this.handleHostnameKeyPress = this.handleHostnameKeyPress.bind(this);
  }

  isPortValid() {
    return (
      /\d+/.test(this.props.port) &&
      +this.props.port > 0 &&
      +this.props.port < 65536
    );
  }

  handlePortChange(e) {
    this.props.onChangePort(e.target.value);
  }

  handlePortKeyPress(e) {
    if (e.keyCode === 13) {
      if (this.isPortValid()) {
        this.props.onSubmitPort(this.props.port);
      }
    }
  }

  handleHostnameKeyPress(e) {
    if (e.keyCode === 13) {
      this.props.onSubmitHostname(this.props.hostname);
    }
  }

  handleHostnameChange(e) {
    this.props.onChangeHostname(e.target.value);
  }

  statusStyle() {
    return {
      display: "block",
      width: "28px",
      height: "28px",
      borderRadius: "50%",
      border: "1px solid #333",
      margin: "4px",
      backgroundColor: this.props.running ? "#0a0" : "red",
    };
  }

  render() {
    return (
      <div className="row">
        <div className="col-xs-2">
          <p>{this.props.caption}</p>
          <div style={this.statusStyle()} />
        </div>
        <div className="col-xs-4">
          <div className="inline_block" style={{ marginRight: "1em"}}>Hostname</div>
          <div className="inline_block">
            <FormGroup
              validationState={this.isPortValid() ? "success" : "error"}
            >
              <FormControl
                type="text"
                value={this.props.hostname}
                placeholder="0.0.0.0"
                disabled={this.props.disabledHostname && "disabled"}
                onChange={this.handleHostnameChange}
                onKeyDown={this.handleHostnameKeyPress}
                />
              {this.props.dirtyHostname && <FormControl.Feedback />}
            </FormGroup>
          </div>
        </div>
        <div className="col-xs-3">
          <div className="data-table">
            <div className="inline_block" style={{ marginRight: "1em" }}>Port</div>
            <div className="inline_block">
              <FormGroup
                validationState={this.isPortValid() ? "success" : "error"}
              >
                <FormControl
                  type="text"
                  value={this.props.port}
                  placeholder="Enter a valid port"
                  disabled={this.props.disabledPort && "disabled"}
                  onChange={this.handlePortChange}
                  onKeyDown={this.handlePortKeyPress}
                />
                {this.props.dirtyPort && <FormControl.Feedback />}
              </FormGroup>
            </div>
            <div className="checkbox">
              <label>
                <input
                  type="checkbox"
                  checked={this.props.autostart}
                  onChange={this.props.onToggleAutostart}
                  disabled={this.props.disabledAutostart && "disabled"}
                />{" "}
                Auto Start
              </label>
            </div>
          </div>
        </div>
        <div className="col-xs-3">
          <div className="data-table">
            <div className="inline_block">
              {this.props.running ? (
                <button
                  type="button"
                  className="btn btn-danger"
                  style={{ marginTop: 20 }}
                  onClick={this.props.onStopService}
                >
                  Stop
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-success"
                  style={{ marginTop: 20 }}
                  onClick={this.props.onStartService}
                >
                  Start
                </button>
              )}
            </div>
            {this.props.extraSettings}
            <div
              className="loader-inner ball-pulse"
              style={{ visibility: this.props.onhold ? "visible" : "hidden" }}
            >
              <div />
              <div />
              <div />
            </div>
          </div>
        </div>
      </div>
    );
  }
}
