define([
  "app",
  "models/device/device"
], function(App, Service) {

  var MediaBrowser = {};

  /**
   * Implementation of the UPnP media browser
   *
   * @class Device.MediaBrowser
   */
  MediaBrowser = Service.extend({
    /**
     * @constructor
     */
    initialize: function() {
      MediaBrowser.__super__.initialize.apply(this, arguments);
    },
  });
  return MediaBrowser;
});
