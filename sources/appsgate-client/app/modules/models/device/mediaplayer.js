define([
    "app",
    "models/device/device",
    "text!templates/program/nodes/mediaPlayerActionNode.html",
    "jstree"
], function(App, Device, ActionTemplate) {

    var MediaPlayer = {};

    /**
     * Implementation of the UPnP media player
     *
     * @class Device.MediaPlayer
     */
    MediaPlayer = Device.extend({
        /**
         * @constructor
         */
        initialize: function() {
            MediaPlayer.__super__.initialize.apply(this, arguments);
            var self = this;

            // setting default friendly name if none exists
            if (this.get("name") === "") {
                this.set("name", this.get("friendlyName"));
                this.sendName();
            }

            // request current volume level
            this.requestVolume();

            // listening for volume value
            dispatcher.on(this.get("ref") , function(volume) {
            console.log("received volume : "+volume);
                self.set("volume", volume);
            });
        },
        /**
         *return the list of available actions
         */
        getActions: function() {
            return ["play", "pause", "resume", "stop", "setVolume"];
        },
        /**
         * return the keyboard code for a given action
         */
        getKeyboardForAction: function(act) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = this.getJSONAction("mandatory");

            switch (act) {
                case "play":
                    $(btn).append("<span data-i18n='services.mediaplayer.keyboard.play-media-action'/>");
                    v.methodName = "play";
                    v.phrase = "services.mediaplayer.language.play-media-action";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "resume":
                    $(btn).append("<span data-i18n='services.mediaplayer.keyboard.resume-media-action'/>");
                    v.methodName = "resume";
                    v.phrase = "services.mediaplayer.language.resume-media-action";
                    $(btn).attr("json", JSON.stringify(v));
                    break;                    
                case "pause":
                    $(btn).append("<span data-i18n='services.mediaplayer.keyboard.pause-media-action'/>");
                    v.methodName = "pause";
                    v.phrase = "services.mediaplayer.language.pause-media-action";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "stop":
                    $(btn).append("<span data-i18n='services.mediaplayer.keyboard.stop-media-action'/>");
                    v.methodName = "stop";
                    v.phrase = "services.mediaplayer.language.stop-media-action";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "setVolume":
                    $(btn).append("<span data-i18n='services.mediaplayer.keyboard.set-volume-media-action'/>");
                    v.methodName = "setVolume";
                    v.phrase = "services.mediaplayer.language.set-volume-media-action";
          			v.args = [ {"type":"int", "value": "0"}];

                    $(btn).attr("json", JSON.stringify(v));
                    break;
                default:
                    console.error("unexpected action found for Media Player: " + act);
                    btn = null;
                    break;
            }
            return btn;
        },
        /**
         * Send a message to the backend to play the current media
         */
        sendPlay: function() {
            var selectedMedia = $("#selectedMedia");
            var url = selectedMedia.attr("url");
            if (typeof url !== 'undefined') {
                console.log("sending play with " + url);
                this.remoteControl("play", [{"type": "String", "value": url}], this.id);
            }
        },
        /**
         * Send a message to the backend to play the current media
         */
        sendResume: function() {
            this.remoteControl("play", [], this.id);
        },
        /**
         * Send a message to the backend to pause the current media
         */
        sendPause: function() {
            this.remoteControl("pause", [], this.id);
        },
        /**
         * Send a message to the backend to stop the media player
         */
        sendStop: function() {
            this.remoteControl("stop", [], this.id);
        },
        /**
         * Send a message to the backend to set the volume to a given level
         */
        sendVolume: function(volume) {
            this.remoteControl("setVolume", [{"type": "int", "value": volume}], this.id);
        },
        /**
         * Sends a request to the server for the current volume level
         */
        requestVolume: function() {
            this.remoteControl("getVolume", [], this.id );
        },
        // Displays a tree of items the player can read
        onBrowseMedia: function(selectedMedia) {
            var browsers = services.getMediaBrowsers();
            var currentDevice;

            // make sure the tree is empty
            $(".browser-container").jstree('destroy');

            var xml_data = "";
            for (var i = 0; i < browsers.length; i++) {
                var name = browsers[i].get("friendlyName") !== "" ? browsers[i].get("friendlyName") : browsers[i].get("id");
                xml_data += "<item id='" + browsers[i].get("id") + "' rel='root'>" + "<content><name>" + name + "</name></content></item>";
            }

            var mediabrowser = $(".browser-container").jstree({
                "xml_data": {
                    data: "<root>" + xml_data + "</root>"
                },
                "themes": {
                    "theme": "apple",
                },
                "unique": {
                    "error_callback": function(n, p, f) {
                        console.log("unique conflict");
                    }
                },
                "types": {
                    "types": {
                        "media": {
                            "valid_children": "none",
                            "icon": {
                                "image": "app/img/drive.png"
                            }
                        },
                    },
                },
                "plugins": ["xml_data", "themes", "types", "crrm", "ui", "unique"]
            }).delegate("a", "click", function(event, data) {
                event.preventDefault();
                var target = "" + event.currentTarget.parentNode.id;
                if (typeof currentDevice === 'undefined' || event.currentTarget.parentNode.getAttribute("rel") === "root") {
                    currentDevice = services.get(target);
                    target = "0";
                }
                if (event.currentTarget.parentNode.getAttribute("rel") !== "media") {
                    $("#media-browser-modal .media-button").addClass("disabled");
                    currentDevice.remoteControl("browse", [{"type": "String", "value": target}, {"type": "String", "value": "BrowseDirectChildren"}, {"type": "String", "value": "*"}, {"type": "long", "value": "0"}, {"type": "long", "value": "0"}, {"type": "String", "value": ""}]);
                }
                else {
                    $("#media-browser-modal .media-button").removeClass("disabled");
                    selectedMedia.text(event.currentTarget.parentNode.attributes.title.textContent);
                    selectedMedia.attr("title", event.currentTarget.parentNode.attributes.title.textContent);
                    selectedMedia.attr("url", event.currentTarget.parentNode.attributes.res.textContent);
                }
            });

            dispatcher.on("mediaBrowserResults", function(result) {
                var D = null;
                var P = new DOMParser();
                if (result !== null && result.indexOf("<empty/>") == -1) {
                    D = P.parseFromString(result, "text/xml");
                    var parentNode;
                    // attaching detected containers to the tree
                    var L_containers = D.querySelectorAll('container');
                    for (var i = 0; i < L_containers.length; i++) {
                        var cont = L_containers.item(i);
                        // making sure to not create duplicates
                        if (!document.getElementById(cont.getAttribute('id'))) {
                            parentNode = document.getElementById(cont.getAttribute('parentID'));
                            $(".browser-container").jstree("create", parentNode, "inside", {"data": {"title": cont.querySelector('title').textContent}, "attr": {"id": cont.getAttribute('id'),
                                    "title": cont.querySelector('title').textContent, "parent_id": cont.getAttribute('parentID'), "rel": 'container'}}, false, true);
                        }
                    }
                    // attaching media items to the tree
                    var L_items = D.querySelectorAll('item');
                    for (i = 0; i < L_items.length; i++) {
                        var item = L_items.item(i);
                        // making sure to not create duplicates
                        if (!document.getElementById("" + item.getAttribute('parentID') + item.getAttribute('id'))) {
                            parentNode = document.getElementById(item.getAttribute('parentID'));
                            $(".browser-container").jstree("create", parentNode, "inside", {"data": {"title": item.querySelector('title').textContent}, "attr": {"id": "" + item.getAttribute('parentID') + item.getAttribute('id'),
                                    "title": item.querySelector('title').textContent, "parent_id": item.getAttribute('parentID'), "rel": 'media', "res": item.querySelector('res').textContent}}, false, true);
                        }
                    }
                }
            });
        },
    /**
     * @returns the action template specific for media player
     */
    getTemplateAction: function() {
      return _.template(ActionTemplate);
    },
    });
    return MediaPlayer;
});
