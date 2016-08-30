'use strict';

var RNSound = require('react-native').NativeModules.RNSound;
var IsAndroid = RNSound.IsAndroid;
var resolveAssetSource = require("react-native/Libraries/Image/resolveAssetSource");
var nextKey = 0;

function isRelativePath(path) {
  return !/^\//.test(path);
}

function Sound(filename, name, onError) {
  this._filename = filename;
  if(!filename.startsWith('exp://')) {
    var asset = resolveAssetSource(filename);
    if (asset) {
      this._filename = asset.uri;
      onError = basePath;
    } else {
      this._filename = basePath ? basePath + '/' + filename : filename;
  
      if (IsAndroid && !basePath && isRelativePath(filename)) {
        this._filename = filename.toLowerCase().replace(/\.[^.]+$/, '');
      }
    }
  }

  this._loaded = false;
  this._key = name;
  this._duration = -1;
  this._numberOfChannels = -1;
  this._volume = 1;
  this._pan = 0;
  this._numberOfLoops = 0;
  console.log(this._filename);
  RNSound.prepare(this._filename, this._key, (error) => {
    if (error === null) {
      this._loaded = true;
    }
    onError && onError(error);
  });
}

Sound.prototype.isLoaded = function() {
  return this._loaded;
};

Sound.prototype.play = function() {
  if (this._loaded) {
    RNSound.play(this._key, this._volume);
  }
  return this;
};

Sound.prototype.pause = function() {
  if (this._loaded) {
    RNSound.pause(this._key);
  }
  return this;
};

Sound.prototype.stop = function() {
  if (this._loaded) {
    RNSound.stop(this._key);
  }
  return this;
};

Sound.prototype.setVolume = function(value) {
  this._volume = value/100.0;
  if (this._loaded) {
    if (IsAndroid) {
      RNSound.setVolume(this._key, this._volume, this._volume);
    } else {
      RNSound.setVolume(this._key, this._volume);
    }
  }
  return this;
};

Sound.prototype.getNumberOfLoops = function() {
  return this._numberOfLoops;
};

Sound.prototype.setNumberOfLoops = function(value) {
  this._numberOfLoops = value;
  if (this._loaded) {
    if (IsAndroid) {
      RNSound.setLooping(this._key, value);
    } else {
      RNSound.setNumberOfLoops(this._key, value);
    }
  }
  return this;
};

Sound.checkExpansionFile = function(version, patch, uri, errCallback) {
  RNSound.checkExpansionFile(version, patch, uri, errCallback);
};

Sound.MAIN_BUNDLE = RNSound.MainBundlePath;
Sound.DOCUMENT = RNSound.NSDocumentDirectory;
Sound.LIBRARY = RNSound.NSLibraryDirectory;
Sound.CACHES = RNSound.NSCachesDirectory;

module.exports = Sound;

