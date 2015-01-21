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
        "click #cancel-edit-button": "onClickCancelEdit",
        "change .lamp-color-picker": "onChangeLampColorNode",
        "change .selector-place-picker": "onChangeSeletorPlaceNode",
        "change .day-forecast-picker": "onChangeDayForecastNode",
        "change .code-forecast-picker": "onChangeCodeForecastNode",
        "change .typical-forecast-picker": "onChangeTypicalForecastNode",
        "change .scale-selector": "onChangeValue",
        "change .comparator-select": "onChangeComparatorNode",
        "change .number-input": "onChangeValue",
        "change .arg-input": "onChangeArgValue",
        "change .volume-input": "onChangeMediaVolume",
        "change .ard-zone-selector": "onARDZoneSelector",
        "change .ard-input-selector": "onARDInputSelector",
        "change .lamp-input-selector": "onLampInputSelector",
        "change .ard-input-value-selector": "onARDInputValueSelector",
        "change .hour-picker, .minute-picker": "onChangeClockValue",
        "change .hour-before-picker, .minute-before-picker, .hour-after-picker, .minute-after-picker": "onChangeClockCheckingValue",
        "change .clock-before-after-picker": "onChangeClockBeforeAfter",

        "click .valid-media": "onValidMediaButton",
        "keyup .programNameInput": "validEditName"
      },
      /**
      * @constructor
      */
      initialize: function() {
        this.Mediator = new Mediator();
        this.Mediator.loadProgramJSON(this.model.get("body"), this.model.get("id"));
        this.bodyJson = $.extend(true, {}, this.model.get("body"));
        this.oldState = this.model.get("runningState");
        console.log(this.oldState);
        this.refreshing = false;


        this.listenTo(this.model, "change", this.refreshDisplay);
        this.listenTo(devices, "remove", this.refreshDisplay);
        this.listenTo(services, "remove", this.refreshDisplay);
        this.listenTo(dispatcher, "refreshDisplay", this.refreshDisplay);
        this.listenTo(dispatcher, "changeArgValue", this.onChangeArgValue2);
      },
      validEditName: function(e) {
        e.preventDefault();

        // update the name if it is ok
        if ((e.keyCode < 9 || e.keyCode > 45) && this.checkProgramName()) {
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
          $("#end-edit-button").addClass("valid-disabled");

          return false;
        }

        // name contains html code
        if (/(&|>|<)/.test($(".programNameInput").val())) {
          $(".text-danger")
                  .text($.i18n.t("edit-name-modal.contains-html"))
                  .removeClass("hide");
          $("#end-edit-button").addClass("disabled");
          $("#end-edit-button").addClass("valid-disabled");

          return false;
        }


        var currentProgramID=this.model.get("id");
        var programsWithSameName=programs.where({name: $(".programNameInput").val()}).filter(function(prog){
              return prog.id != null && prog.id!=currentProgramID;
          });
        // name already existing
        if (programsWithSameName.length > 0) {
          $(".text-danger").removeClass("hide");
          $(".text-danger").text($.i18n.t("modal-edit-program.program-already-existing"));
          $("#end-edit-button").addClass("disabled");
          $("#end-edit-button").addClass("valid-disabled");

          return false;
        }

        //ok
        $(".text-danger").addClass("hide");
        $("#end-edit-button").removeClass("disabled");

        return true;
      },
      onLampInputSelector: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var valueInt1 = $("#lamp-time-" + iid)[0].value;
        var valueInt2 = $("#lamp-frequency-" + iid)[0].selectedOptions[0].value;
        var value1 = {"type": "long", "value": valueInt1};
        var value2 = {"type": "long", "value": valueInt2};
        this.Mediator.setNodeArg(iid, 0, value1);
        this.Mediator.setNodeArg(iid, 1, value2);
      },
      //ARD
      onARDInputSelector: function(e) {
            e.stopPropagation();
            var iid = $(e.currentTarget).attr("target-id");
            var valueInt = $("#ard-input-" + iid)[0].selectedOptions[0].value;
            var value = {"type": "int", "value": valueInt};
            this.Mediator.setNodeArg(iid, 0, value);
      },
      onARDInputValueSelector: function(e) {
            e.stopPropagation();
            var iid = $(e.currentTarget).attr("target-id");
            var valueBoolean = $("#ard-input-value-" + iid)[0].selectedOptions[0].value;
            var value = {"type": "boolean", "value": valueBoolean};
            this.Mediator.setNodeArg(iid, 1, value);
      },
      onARDZoneSelector: function(e) {
            e.stopPropagation();
            var iid = $(e.currentTarget).attr("target-id");
            var valueInt = $("#ard-zone-" + iid)[0].selectedOptions[0].value;
            var value = {"type": "int", "value": valueInt};
            this.Mediator.setNodeArg(iid, 0, value);
        },
      onClickEndEdit: function(e) {
        this.model.set("body", this.Mediator.programJSON);
        this.model.set("modified", false);
        this.model.set("isNew", "false");
        if (this.Mediator.isValid) {
          this.model.set("runningState", "DEPLOYED");
        } else {
          this.model.set("runningState", "INVALID");
        }
        this.model.save();
        appRouter.navigate("#programs/" + this.model.get("id"), {trigger: true});
      },
      onClickCancelEdit: function(e) {
        if(this.model.get("isNew") === "true"){
          this.model.destroy();
          appRouter.navigate("#programs", {trigger: true});
        } else{
          this.model.set("body", this.bodyJson);
          this.model.set("runningState", this.oldState);
          appRouter.navigate("#programs", {trigger: true});
          appRouter.navigate("#programs/" + this.model.get("id"), {trigger: true});
        }
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
        dispatcher.trigger("refreshDisplay");
      },
      /**
       * Method to handle event on a button click in the input area
       */
      onClickProg: function(e) {
        // checking what kind of button was clicked
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
          // checking if the node has to be deleted or selected
          if ($(button).hasClass("glyphicon-trash")) {
            this.Mediator.removeNode(button.id);
          } else {
            this.Mediator.setCurrentPos(button.id);
            dispatcher.trigger("refreshDisplay");
          }
        }
      },
      // Displays a tree of items the player can read
      onBrowseMedia: function(selectedMedia) {
        var self = this;
        var browsers = devices.getMediaBrowsers();
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
            currentDevice = devices.get(target);
            target = "0";
          }
          if (event.currentTarget.parentNode.getAttribute("rel") !== "media") {
            $("#media-browser-modal .media-button").addClass("disabled");
            currentDevice.remoteControl("browse", [{"type": "String", "value": target}, {"type": "String", "value": "BrowseDirectChildren"}, {"type": "String", "value": "*"}, {"type": "long", "value": "0"}, {"type": "long", "value": "0"}, {"type": "String", "value": ""}]);
          }
          else {
            $("#media-browser-modal .media-button").removeClass("disabled");
            self.Mediator.setNodeAttribute($("#media-browser-modal").attr("target-iid"), "args", [{type: "String", value: event.currentTarget.parentNode.attributes.res.value},{type: "String", value: event.currentTarget.textContent}]);
            self.Mediator.setNodeAttribute($("#media-browser-modal").attr("target-iid"), "fileName", event.currentTarget.textContent);
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
      onChangeSeletorPlaceNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var value = e.currentTarget.selectedOptions[0].value;
        this.Mediator.setNodeAttribute(iid, "where", [{"type":"string","value":""+value}]);

        // clearing selection
        this.resetSelection();
      },

      onChangeDayForecastNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newDay = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "int", "value": newDay};
        this.Mediator.setNodeArg(iid, 0, value);

        // // clearing selection
        // this.resetSelection();
      },
      onChangeCodeForecastNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newCode = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "int", "value": newCode};
        var i = 0;
          $(".day-forecast-picker").each(function(){
              if (this.getAttribute("target-id") === iid) {
                  i = 1;
              }
          });

        this.Mediator.setNodeArg(iid, i, value);

        // // clearing selection
        // this.resetSelection();
      },
      onChangeTypicalForecastNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newTypical = e.currentTarget.selectedOptions[0].value;
        var value = {"type": "int", "value": newTypical};
        var i = 0;
          $(".typical-forecast-picker").each(function(){
              if (this.getAttribute("target-id") === iid) {
                  i = 1;
              }
          });

        this.Mediator.setNodeArg(iid, i, value);

        // // clearing selection
        // this.resetSelection();
      },
      onChangeComparatorNode: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var newOp = e.currentTarget.selectedOptions[0].value;
        this.Mediator.setNodeAttribute(iid, "comparator", newOp);

        // // clearing selection
        this.resetSelection();
      },
      onChangeValue: function(e) {
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
      onChangeArgValue2: function(iid, index, v) {
        var value = {"type": "String", "value": v};
        this.Mediator.setNodeArg(iid, index, value);
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

      onChangeClockCheckingValue: function(e) {
        e.stopPropagation();

        var iid = $(e.currentTarget).attr("target-id");

        if($(e.currentTarget).attr("class") === "hour-before-picker" || $(e.currentTarget).attr("class") === "minute-before-picker") {

          var hourValue = $("#clock-before-hour-" + iid)[0].selectedOptions[0].value;
          var minuteValue = $("#clock-before-minute-" + iid)[0].selectedOptions[0].value;

          var value = {"type": "long", "value": (hourValue*60*60*1000)+(minuteValue*60*1000)};
          this.Mediator.setNodeArg(iid, 1, value);

        } else if ($(e.currentTarget).attr("class") === "hour-after-picker" || $(e.currentTarget).attr("class") === "minute-after-picker") {

          var hourValue = $("#clock-after-hour-" + iid)[0].selectedOptions[0].value;
          var minuteValue = $("#clock-after-minute-" + iid)[0].selectedOptions[0].value;

          var value = {"type": "long", "value": (hourValue*60*60*1000)+(minuteValue*60*1000)};
          this.Mediator.setNodeArg(iid, 0, value);
        }

        if($("#clock-before-after-" + iid)[0].selectedOptions[0].value === "devices.clock.language.checkCurrentTimeOfDay.after"){
          this.Mediator.setNodeArg(iid, 1, undef);
        } else if($("#clock-before-after-" + iid)[0].selectedOptions[0].value === "devices.clock.language.checkCurrentTimeOfDay.before"){
          this.Mediator.setNodeArg(iid, 0, undef);
        }

        // clearing selection
        this.resetSelection();
      },
      onChangeClockBeforeAfter: function(e) {
        e.stopPropagation();
        var iid = $(e.currentTarget).attr("target-id");
        var undef = {"type": "long", "value": -1};
        this.Mediator.setNodeAttribute(iid, "phrase", $(e.currentTarget)[0].selectedOptions[0].value);
        if($(e.currentTarget)[0].selectedOptions[0].value === "devices.clock.language.checkCurrentTimeOfDay.before") {
          this.Mediator.setNodeArg(iid, 0, undef);
          var hourValue = $("#clock-before-hour-" + iid)[0].selectedOptions[0].value;
          var minuteValue = $("#clock-before-minute-" + iid)[0].selectedOptions[0].value;

          var value = {"type": "long", "value": (hourValue*60*60*1000)+(minuteValue*60*1000)};
          this.Mediator.setNodeArg(iid, 1, value);
        } else if ($(e.currentTarget)[0].selectedOptions[0].value === "devices.clock.language.checkCurrentTimeOfDay.after"){
          var hourValue = $("#clock-after-hour-" + iid)[0].selectedOptions[0].value;
          var minuteValue = $("#clock-after-minute-" + iid)[0].selectedOptions[0].value;

          var value = {"type": "long", "value": (hourValue*60*60*1000)+(minuteValue*60*1000)};
          this.Mediator.setNodeArg(iid, 0, value);
          this.Mediator.setNodeArg(iid, 1, undef);
        }
        this.resetSelection();
      },
      resetSelection: function() {
        $(".expected-elements").html("");
        this.Mediator.setCurrentPos(-1);
        //this.Mediator.buildInputFromJSON();
        this.refreshDisplay();
      },
      refreshDisplay: function(e) {
        if (this.refreshing) {
          return;
        }
        $(".programInput").clearQueue();
        if (typeof e === "undefined" || ((typeof e.attributes != "undefined") && e.attributes["type"] !== 21)) {
          this.refreshing = true;
          this.Mediator.buildInputFromJSON();
          this.Mediator.buildKeyboard();
          this.model.set("runningState", this.Mediator.programState.toUpperCase());
          this.applyEditMode();
          // translate the view
          this.$el.i18n();
            $("#prog-led").attr("title",  this.Mediator.getProgramState());
            $("#prog-led").attr("class", "pull-left led-"+this.Mediator.programState);
          if (this.Mediator.programState == "invalid") {
            $(".programNameInput").removeClass("valid-program");
          } else {
            $(".programNameInput").addClass("valid-program");
          }

          // scrolling to the selected node
          if($(".selected-node").length > 0) {
            var focusPosition = $(".selected-node").offset().top + $(".programInput").scrollTop() - $(".programInput").offset().top;
            if($(".programInput").height() > $(".selected-node").height()) {
              focusPosition -= $(".programInput").height()/2 - $(".selected-node").height()/2;
            }
            $(".programInput").animate({scrollTop: focusPosition}, 1000);
          }

          $( document ).tooltip();

          this.refreshing = false;
        }
      },
      applyEditMode: function() {
        if (this.Mediator.currentNode === -1 && this.Mediator.lastAddedNode !== null) {
          var nextInput = this.Mediator.findNextInput($(".programInput").find("#" + this.Mediator.lastAddedNode.iid));

          this.Mediator.setCursorAndBuildKeyboard(parseInt(nextInput.nextAll(".input-spot").attr("id")));
          console.log("nextInput : "+nextInput.nextAll(".input-spot").attr("id"));
        }
        // if no input point is chosen at this point, we select the last empty element
        if ($(".expected-elements").children().length === 0) {
          var lastInputPoint = $(".programInput").find(".input-spot").last();
          this.Mediator.setCursorAndBuildKeyboard(parseInt(lastInputPoint.attr("id")));
          console.log("lastInputPoint : "+lastInputPoint);
        }

        $(".programInput").find(".selected-node").removeClass("selected-node");
        $("#" + parseInt(this.Mediator.currentNode)).addClass("selected-node");

        $(".input-spot-then").removeClass("input-spot-then");
        $(".input-spot").prev().children(".btn-and, .btn-then").addClass("input-spot-then");

        $(".programInput").find(".progress-indicator-group").addClass("hidden");

        //disabling not clickable buttons
        if($(".programInput").find(".mandatory-spot").length > 0){
            $(".input-spot:not(.mandatory-spot:first)").addClass("disabled");
        }

        // adding tooltips and changing style for the inactive nodes after a self-stop
        $(".programInput").find(".btn-prog-stopself").parent().nextAll(".btn-current").children(".btn-prog:not(.btn-trash)").attr("title",$.i18n.t("programs.inactive-node")).addClass("inactive-node");
        $(".programInput").find(".btn-prog-stopself").parent().nextAll(".input-spot").attr("title",$.i18n.t("programs.inactive-node")).addClass("inactive-node");

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
          this.resize($(".scrollable"));
        }

        return this;
      }

    });
    return ProgramEditorView;
  });
