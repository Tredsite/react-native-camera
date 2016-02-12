'use strict';

var React = require('react-native');
var { requireNativeComponent, PropTypes, View, ReactNativeAttributePayload } = React;
var ReactNativeAttributePayload=require('ReactNativeAttributePayload');
var NativeAndroidCameraView = requireNativeComponent('CameraViewAndroid', AndroidCameraView
);

var merge = require('merge');

class AndroidCameraView extends React.Component {
  constructor() {
    super();
    this._onChange = this._onChange.bind(this);
    this.onCaptureCompleted = null;
    this.orientationMode = "";
  }

  _onChange(event: Event) {
    if (event.nativeEvent.type == "camera_capture") {
      if (this.onCaptureCompleted) {
        this.onCaptureCompleted(null, event.nativeEvent.message);
        this.onCaptureCompleted = null;
      }
    } else if (event.nativeEvent.type == "barcode_capture") {
      if (this.props.onBarCodeRead) {
        this.props.onBarCodeRead(event.nativeEvent.message);
      }
    } else if (event.nativeEvent.type == "orientation_changed") {
      if (this.props.onOrientationChanged) {
        this.props.onOrientationChanged({ orientation: event.nativeEvent.portraitMode ? 'portrait' : 'landscape' });
        if (event.nativeEvent.portraitMode) {
          this.orientationMode = "portrait";
        } else {
          this.orientationMode = "landscape";
        }
      }
    }
  }

  componentDidMount() {
    this._root.setNativeProps({
      startCamera: true
    });
  }

  componentWillUnmount() {
    this._root.setNativeProps({
      startCamera: false
    });
  }

  capture(callback) {
    this.onCaptureCompleted = callback;
    this._root.setNativeProps({
      startCapture: this.orientationMode
    });
  }

  toggleTorch(mode) {
    this._root.setNativeProps({
      torchMode: mode
    });
  }

  render() {
    return (
      <NativeAndroidCameraView
    ref={component => this._root = component}
    {...this.props} onChange={this._onChange}
    values={this.props.values} selected={this.props.selected} />
  );
  }
}

AndroidCameraView.propTypes = {
  ...View.propTypes,
  startCapture: PropTypes.string,
  startCamera: PropTypes.bool,
  onBarCodeRead: PropTypes.func,
  onOrientationChanged: PropTypes.func,
  torchMode:PropTypes.bool,
};

AndroidCameraView.defaultProps = {
  onBarCodeRead: null,
  startCapture: "",
  startCamera: false,
  onOrientationChanged: null,
  torchMode: true
};

module.exports = AndroidCameraView;
