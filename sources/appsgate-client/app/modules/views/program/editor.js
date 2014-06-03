define([
  "app",
  "modules/mediator",
  "text!templates/program/editor/editor.html"
  ], function(App, Mediator, programEditorTemplate) {

    var ProgramEditorView = {};
    /**
    * Render the editor view
    */
    ProgramEditorView = Backbone.View.extend({
      tplEditor: _.template(programEditorTemplate),
      events: {
        "mouseup .btn-keyboard": "onClickKeyboard",
        "mouseup .btn-prog": "onClickProg",
        "click #end-edit-button": "onClickEndEdit",
        "change .lamp-color-picker": "onChangeLampColorNode",
        "change .weather-town-picker": "onChangeTownNode",
        "change .day-forecast-picker": "onChangeDayForecastNode",
        "change .code-forecast-picker": "onChangeCodeForecastNode",
        "change .number-input": "onChangeNumberValue",
        "change .arg-input": "onChangeArgValue",
        "change .volume-input": "onChangeMediaVolume",
        "change .hour-picker, .minute-picker": "onChangeClockValue",
        "click .valid-media": "onValidMediaButton",
        "keyup .programNameInput": "validEditName"
      },
      /**
      * @constructor
      */
      initialize: function() {
        this.Mediator = new Mediator();
        this.Mediator.loadProgramJSON(this.model.get("body"));

        this.listenTo(this.model, "change", this.refreshDisplay);
        this.listenTo(devices, "change", this.refreshDisplay);
        this.listenTo(services, "change", this.refreshDisplay);
        this.listenTo(dispatcher, "refreshDisplay", this.refreshDisplay);
      },
      validEditName: function(e) {
        e.preventDefault();

        // update the name if it is ok
        if (this.checkProgramName()) {
          // set the new name to the place
          this.model.set("name", $(".programNameInput").val());
        }
      },
      /**
      * Check the current value of the input text and show a message error if needed
      *
      * @return false if the typed name already exists, true otherwise
      */
      checkProgramName: function() {
        // name is empty
        if ($(".programNameInput").val() === "") {
          $(".text-danger").removeClass("hide");
          $(".text-danger").text($.i18n.t("modal-edit-program.program-name-empty"));
          $("#end-edit-button").addClass("disabled");

          return false;
        }

        // name already existing
        if (programs.where({name: $(".programNameInput").val()}).length > 0) {
          $(".text-danger").removeClass("hide");
          $(".text-danger").text($.i18n.t("modal-edit-program.program-already-existing"));
          $("#end-edit-button").addClass("disabled");

          return false;
        }

        //ok
        $(".text-danger").addClass("hide");
        $("#end-edit-button").removeClass("disabled");

        return true;
      },
      onClickEndEdit: function(e) {
        this.model.set("body", this.Mediator.programJSON);
        this.model.set("modified", false);
        if (this.Mediator.isValid) {
          this.model.set("runningState", "DEPLOYED");
        } else {
          this.model.set("runningState", "INVALID");
        }
        this.model.save();
        appRouter.navigate("#programs/" + this.model.get("id"), {trigger: true});
      },
      /**
       * Method to handle event on a button on the keyboard
       */
      onClickKeyboard: function(e) {
        button = e.target;
        // the event may be catched by a span or a div contained by the button
        while (button !== null && typeof button.classList === 'undefined' || !button.classList.contains('btn-keyboard')) {
          button = button.parentNode;
        }
        this.Mediator.addNodeFromButton(button);
      },
      /**
       *
       */
      onClickProg: function(e) {
        button = e.target;
        if (button !== null && typeof button.classList !== 'undefined' && (button.classList.contains('btn-media-choice') || button.classList.contains('default-media-choice'))) {
          e.stopPropagation();
          this.onBrowseMedia($(button));
        }
        else if (button.tagName.toUpperCase() !== "SELECT" && button.tagName !== "INPUT"  && button.tagName !== "TEXTAREA"){
          while (button !== null && button.id  === '') {
            button = button.parentNode;
          }
          if (button.id ==="") {
            // clicking on a "et" button
            // do nothing
            return;
          }
          if ($(button).hasClass("glyphicon-trash")) {
            this.Mediator.removeNode(button.id);
          } else {
            this.Mediator.setCurrentPos(button.id);
            this.refreshDisplay();
          }
        }
      },
      // Displays a tree of items the player can read
      onBrowseMedia: function(selectedMedia) {
        var self = this;
        var browsers = services.getMediaBrowsers();
        var currentDevice;

        // make sure the tree is empty
        $(".browser-container").jstree('destroy');
        $("#media-browser-modal").attr("target-iid", selectedMedia.attr("target-iid"));
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
            self.Mediator.setNodeAttribute($("#media-browser-modal").attr("target-iid"), "args", [{type: "String", value: event.currentTarget.parentNode.attributes.res.textContent}]);
            self.Mediator.setNodeAttribute($("#media-browser-modal").attr("target-iid"), "fileName", event.currentTarget.parentNode.attributes.title.textContent);
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
      onValidMediaButton: function() {
        $("#media-browser-modal").modal("hide");
        this.refreshDisplay();
      },
      onChangeMediaVolume: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var value = e.currentTarget.value;

        this.Mediator.setNodeAttribute(iid, "args", [{type: "int", value: parseInt(e.currentTarget.value)}]);
        this.Mediator.setNodeAttribute(iid, "volume", e.currentTarget.value);

        // clearing selection
        this.resetSelection();
      },
      onChangeLampColorNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var value = e.currentTarget.selectedOptions[0].value;
        this.Mediator.setNodeAttribute(iid, "methodName", value);

        // clearing selection
        this.resetSelection();
      },
      onChangeTownNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newTown = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "String", "value": newTown};
        this.Mediator.setNodeArg(iid, 0, value);

        // clearing selection
        // this.resetSelection();
      },
      onChangeDayForecastNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newDay = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "int", "value": newDay};
        this.Mediator.setNodeArg(iid, 1, value);

        // // clearing selection
        // this.resetSelection();
      },
      onChangeCodeForecastNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newCode = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "int", "value": newCode};
        this.Mediator.setNodeArg(iid, 2, value);

        // // clearing selection
        // this.resetSelection();
      },
      onChangeNumberValue: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var value = e.currentTarget.value;
        this.Mediator.setNodeAttribute(iid, "value", value);
        // clearing selection
        this.resetSelection();
      },
      onChangeArgValue: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var value = {"type": "String", "value": e.currentTarget.value};
        var index = $(e.currentTarget).attr("target-index")
        this.Mediator.setNodeArg(iid, index, value);
        // clearing selection
        this.resetSelection();
      },
      onChangeClockValue: function(e) {
        e.stopPropagation();

        var iid = $(e.currentTarget).attr("target-id");

        var debugH = $("#clock-hour-" + iid);
        var debugM = $("#clock-minute-" + iid);
        var hourValue = $("#clock-hour-" + iid)[0].selectedOptions[0].value;
        var minuteValue = $("#clock-minute-" + iid)[0].selectedOptions[0].value;

        this.Mediator.setNodeAttribute(iid, "eventValue", devices.getCoreClock().getClockAlarm(hourValue, minuteValue));

        // clearing selection
        this.resetSelection();
      },
      resetSelection: function() {
        $(".expected-elements").html("");
        this.Mediator.setCurrentPos(-1);
        //this.Mediator.buildInputFromJSON();
        this.refreshDisplay();
      },
      refreshDisplay: function(e) {
        //if (e == undefined || ((typeof e.attributes != "undefined") && e.attributes["type"] !== 21)) {
        if (typeof e === "undefined" || ((typeof e.attributes != "undefined") && e.attributes["type"] !== 21)) {
          this.Mediator.buildInputFromJSON();
          this.Mediator.buildKeyboard();
          if (!this.Mediator.isValid) {
            this.model.set("runningState", "INVALID");
          }
          this.applyEditMode();
          // translate the view
          this.$el.i18n();
        if (this.Mediator.isValid) {
          this.model.set("runningState", "DEPLOYED");
          $(".led").addClass("led-default").removeClass("led-red");
          $(".programNameInput").addClass("valid-program");
        } else {
          this.model.set("runningState", "INVALID");
          $(".led").addClass("led-red").removeClass("led-default");
          $(".programNameInput").removeClass("valid-program");
        }
        }
      },
      applyEditMode: function() {
        if (this.Mediator.currentNode === -1 && this.Mediator.lastAddedNode !== null) {
          var nextInput = this.Mediator.findNextInput($(".programInput").find("#" + this.Mediator.lastAddedNode.iid).parent());

          this.Mediator.setCursorAndBuildKeyboard(parseInt(nextInput.nextAll(".input-spot").attr("id")));
          console.log("nextInput : "+nextInput.nextAll(".input-spot").attr("id"));
        }
        // if no input point is chosen at this point, we select the last empty element
        if ($(".expected-elements").children().length === 0) {
          var lastInputPoint = $(".programInput").children(".input-spot").last();
          this.Mediator.setCursorAndBuildKeyboard(parseInt(lastInputPoint.attr("id")));
          console.log("lastInputPoint : "+lastInputPoint);
        }

        $(".programInput").find(".selected-node").removeClass("selected-node");
        $("#" + parseInt(this.Mediator.currentNode)).addClass("selected-node");
      },
      /**
      * Render the editor view
      */
      render: function() {

        var self = this;

        // render the editor with the program
        this.$el.append(this.tplEditor({
          program: this.model
        }));

        if (this.model) {
          this.refreshDisplay();

          // fix the programs list size to be able to scroll through it
          this.resizeDiv($(self.$el.find(".editorWorkspace")[0]), true);
        }

        return this;
      }

    });
    return ProgramEditorView;
  });
