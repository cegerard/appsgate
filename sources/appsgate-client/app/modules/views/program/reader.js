define([
  "app",
  "modules/mediator",
  "text!templates/program/reader/reader.html"
  ], function(App, Mediator, programEditorTemplate) {

    var ProgramReaderView = {};
    /**
    * Render the editor view
    */
    ProgramReaderView = Backbone.View.extend({
      tplEditor: _.template(programEditorTemplate),
      events: {
        "shown.bs.modal #schedule-program-modal": "initializeModal",
        "shown.bs.modal #test-program-modal": "initializeProgramTestModal",
        "hide.bs.modal": "toggleModalValue",
        "click #schedule-program-modal button.valid-button": "validScheduleProgram",
        "click #test-program-modal button.valid-button": "launchProgramTest",
        "click #stop-testing-button": "cancelTesting",
        "click button.start-program-button": "onStartProgramButton",
        "click button.stop-program-button": "onStopProgramButton",
        "click button.cancel-edit-program-button": "onCancelEditProgram",
        "click button.launch-edit-program-button": "onLaunchEditProgram",
        "click button.edit-popover-button": "onClickEditProgram",
        "click button.delete-program-button": "onDeleteProgramButton",
        "click button.delete-popover-button": "onClickDeleteProgram",
        "click button.cancel-delete-program-button": "onCancelDeleteProgram",
        "click button.open-calendar-button":"openCalendar",
      },
      /**
      * @constructor
      */
      initialize: function() {
        this.Mediator = new Mediator();
        if(typeof this.model !== "undefined"){
          this.Mediator.loadProgramJSON(this.model.get("body"));
          this.listenTo(this.model, "change", this.refreshDisplay);
        }
        this.Mediator.readonly = true;

        this.listenTo(devices, "change", this.refreshDisplay);
        this.listenTo(services, "change", this.refreshDisplay);
        this.listenTo(dispatcher, "refreshDisplay", this.refreshDisplay);

        this.stopListening(devices.getCoreClock());
        this.listenTo(devices.getCoreClock(), "change", this.displayClockPopover);

      },
      close:function() {
        ProgramReaderView.__super__.close.apply(this, arguments);

        $(".popover").remove();
      },
      /**
       * Clear the input text, hide the error message, check the checkbox and disable the valid button by default
       */
      initializeModal: function() {
          // tell the router that there is a modal
          appRouter.isModalShown = true;
      },
      /**
       * Tell the router there is no modal anymore
       */
      toggleModalValue: function() {
          appRouter.isModalShown = false;
      },
      initializeProgramTestModal: function() {
        var coreClock = devices.getCoreClock();

        // initialize the field to edit the core clock
        $("#test-program-modal select#hour").val(coreClock.get("moment").hour());
        $("#test-program-modal select#minute").val(coreClock.get("moment").minute());
        $("#test-program-modal input#time-flow-rate").val(coreClock.get("flowRate"));

        this.initializeModal();
      },
      /**
       * Check if the name of the program does not already exist. If not, create the program
       * Hide the modal when done
       *
       * @param e JS event
       */
      validScheduleProgram: function(e) {
          var self = this;

          if($("input[name='schedule-radio']:checked").val() == 'activate') {
            this.model.scheduleProgram(true,false);
          } else if ($("input[name='schedule-radio']:checked").val() == 'deactivate') {
            this.model.scheduleProgram(false,true);
          } else {
            this.model.scheduleProgram(true,true);
          }

          // hide the modal
          $("#schedule-program-modal").modal("hide");

          // instantiate the program and add it to the collection after the modal has been hidden
          $("#schedule-program-modal").on("hidden.bs.modal", function() {
            // tell the router there is no modal any more
            appRouter.isModalShown = false;

            $("#schedule-program-modal").off("hidden.bs.modal");

            window.open("https://www.google.com/calendar");

          });
      },
      launchProgramTest: function(e) {
        var self = this;

        // hide the modal
        $("#test-program-modal").modal("hide");

        var coreClock = devices.getCoreClock();
        coreClock.set("simulated",true);

        coreClock.get("moment").set("hour", parseInt($("#test-program-modal select#hour").val()));
        coreClock.get("moment").set("minute", parseInt($("#test-program-modal select#minute").val()));
        // retrieve the value of the flow rate set by the user
        var timeFlowRate = $("#test-program-modal input#time-flow-rate").val();

        // update the attributes hour and minute
        coreClock.set("hour", coreClock.get("moment").hour());
        coreClock.set("minute", coreClock.get("moment").minute());

        //send the update to the server
        coreClock.save();

        // update the attribute time flow rate
        coreClock.set("flowRate", timeFlowRate);

        //send the update to the server
        coreClock.save();

        // instantiate the program and add it to the collection after the modal has been hidden
        $("#test-program-modal").on("hidden.bs.modal", function() {
          // tell the router there is no modal any more
          appRouter.isModalShown = false;

          // starting the program
          self.model.remoteCall("callProgram", [{type: "String", value: self.model.get("id")}]);

          // refresh the menu
          self.render();

          $("#test-program-button").addClass("hidden");
          $("#stop-testing-button").removeClass("hidden");

        });
      },
      cancelTesting: function() {

        $("#test-program-button").removeClass("hidden");
        $("#stop-testing-button").addClass("hidden");

        var coreClock = devices.getCoreClock();
        if(coreClock.get("simulated")){
          coreClock.resetClock();
          coreClock.set("simulated", false);
        }
      },
      displayClockPopover: function() {
        var self = this;
        var coreClock = devices.getCoreClock();
        if(coreClock.get("simulated") == true && (typeof this.testPopoverShown == "undefined" || this.testPopoverShown == false)) {
          this.testPopoverShown = true;

          _.defer(function(){
            // create the test popover
            self.$el.find("#test-button-popover").popover({
                content: $.i18n.t("programs.simulated-time"),
                template: "<div class='popover' role='tooltip'><div class='arrow'></div><div id='popover-clock' class='popover-content'></div></div>",
                placement: "top",
            });

            // show the popup
            self.$el.find("#test-button-popover").popover('show');
          });
        }
        else if(coreClock.get("simulated") == false && this.testPopoverShown == true){
          this.testPopoverShown = false;
          // hide the popup
          this.$el.find("#test-button-popover").popover('destroy');
          $("#popover-clock").parent().remove();
        }

        $("#popover-clock").html("<i class='glyphicon glyphicon-time'></i><span>" + coreClock.get('hour') + ":" + coreClock.get('minute') + ":" + coreClock.get('second') + "</span>");
      },
      openCalendar: function(e) {
          window.open("https://www.google.com/calendar");
      },
      /**
      * Callback to start a program
      *
      * @param e JS mouse event
      */
      onStartProgramButton: function(e) {
        e.preventDefault();

        // get the program to start
        var program = programs.get($(e.currentTarget).attr("id"));

        program.remoteCall("callProgram", [{type: "String", value: program.get("id")}]);

        // refresh the menu
        this.render();

        return false;
      },
      /**
      * Callback to stop a program
      *
      * @param e JS mouse event
      */
      onStopProgramButton: function(e) {
        e.preventDefault();

        // get the program to stop
        var program = programs.get($(e.currentTarget).attr("id"));

        program.remoteCall("stopProgram", [{type: "String", value: program.get("id")}]);
        // refresh the menu
        this.render();

        return false;
      },
      /**
      * Callback when the user has clicked on the button to cancel the deleting. Return to the program
      */
      onCancelDeleteProgram : function() {
        // destroy the popover
        this.$el.find("#delete-program-popover").popover('destroy');
      },
      /**
      * Callback when the user has clicked on the button to remove a program. Remove the program
      */
      onDeleteProgramButton: function() {
        // delete the program
        this.model.destroy();

        // navigate to the list of programs
        appRouter.navigate("#programs", {trigger: true});
      },
      /**
      * Callback when the user has clicked on the button delete.
      */
      onClickDeleteProgram : function(e) {
        var self = this;
        // create the popover
        this.$el.find("#delete-program-popover").popover({
            html: true,
            title: $.i18n.t("programs.warning-program-delete"),
            content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-delete-program-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-danger delete-program-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
            placement: "bottom"
        });
        // listen the hide event to destroy the popup, because it is created to every click on Edit
        this.$el.find("#delete-program-popover").on('hidden.bs.popover', function () {
            self.onCancelDeleteProgram();
        });
        // show the popup
        this.$el.find("#delete-program-popover").popover('show');
      },
      /**
      * Callback when the user has clicked on the button to cancel the launching of edition. Return to the program
      */
      onCancelEditProgram : function() {
        // destroy the popover
        this.$el.find("#edit-program-popover").popover('destroy');
      },
      /**
      * Callback when the user has clicked on the button edit and confirm it. Go to the editor
      */
      onLaunchEditProgram : function() {
        // navigate to the editor
        appRouter.navigate("#programs/editor/" + this.model.get('id'), {trigger: true});
      },
      /**
      * Callback when the user has clicked on the button edit. Go to the editor or show the popup
      */
      onClickEditProgram : function(e) {
        var self = this;
        // if program waiting, show the popup warning
        if (this.model.isWorking()) {
            // create the popover
            this.$el.find("#edit-program-popover").popover({
                html: true,
                title: $.i18n.t("programs.warning-program-edition"),
                content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-edit-program-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-primary launch-edit-program-button'>" + $.i18n.t("programs.edit-program") + "</button></div>",
                placement: "bottom"
            });
            // listen the hide event to destroy the popup, because create to every click on Edit
            this.$el.find("#edit-program-popover").on('hidden.bs.popover', function () {
                self.onCancelEditProgram();
            });
            // show the popup
            this.$el.find("#edit-program-popover").popover('show');
        } else {
            // else go to edit directly
            this.onLaunchEditProgram();
        }
      },
      refreshDisplay: function(e) {
        // To avoid to refresh the whole page at each second
        if (e!== undefined && e.updateClockValue !== undefined) {
          return;
        }
        var input = this.Mediator.getInputFromJSON();
        if (!this.Mediator.isValid) {
          this.model.set("runningState", "INVALID");
        }
        var self = this;
        _.defer(function() {
          input = self.applyReadMode(input);
          $(".programInput").html(input).addClass("read-only");

          if($(".programInput").children(".seq-block-node").children(":not(.input-spot):not(.seq-block-header)").length < 1){
            $(".programInput").children(".seq-block-node").remove();
            $(".programInput").children(".separator").remove();
          }
          else {
            if($(".seq-block-node").find(".input-spot").next(".separator").length > 0
            && $(".seq-block-node").find(".input-spot").next(".separator").next().length == 0){
              $(".seq-block-node").find(".input-spot").next(".separator")[0].remove();
            }
            $(".seq-block-node").find(".input-spot").prev(".separator").remove();
          }
          if($(".programInput").children(".set-block-node").children(":not(.input-spot):not(.set-block-header)").length < 1){
            $(".programInput").children(".set-block-node").remove();
            $(".programInput").children(".separator").remove();
          }
          else {
            if($(".set-block-node").find(".input-spot").next(".separator").length > 0
              && $(".set-block-node").find(".input-spot").next(".separator").next().length == 0){
              $(".set-block-node").find(".input-spot").next(".separator")[0].remove();
            }
            $(".set-block-node").find(".input-spot").prev(".separator").remove();
          }

          $(".input-spot:not(.mandatory-spot)").remove();
          $(".mandatory-spot").text($.i18n.t("language.mandatory-readonly"));

          var test = $(".while-keep-then").parent().next();

          if($(".while-keep-then").parent().next().hasClass("secondary-block-node")) {
            $(".while-keep-then").remove();
          }
          $(".secondary-block-node").remove();

          // adding tooltips and changing style for the inactive nodes after a self-stop
          $(".programInput").find(".btn-prog-stopself").parent().nextAll(".btn-current").children(".btn-prog:not(.btn-trash)").attr("title",$.i18n.t("programs.inactive-node")).addClass("inactive-node");

          if(typeof self.model !== "undefined"){
            $("#led-" + self.model.get("id")).attr("class", "pull-left led-"+self.model.getState());
            $("#led-" + self.model.get("id")).attr("title", $.i18n.t('programs.state.'+self.model.getState()));
            $("#current-led-" + self.model.get("id")).attr("class", "pull-left led-"+self.model.getState());
            $("#current-led-" + self.model.get("id")).attr("title", $.i18n.t('programs.state.'+self.model.getState()));

            if (self.model.isWorking()) {
              $(".start-program-button").hide();
              $(".stop-program-button").show();
              // make the visible button first in the div so the correct style applies
              $(".stop-program-button").insertBefore($(".start-program-button"));
            } else if (self.model.get("runningState") === "INVALID"){
              $(".start-program-button").show();
              $(".start-program-button").prop('disabled', true);
              $(".stop-program-button").hide();
              // make the visible button first in the div so the correct style applies
              $(".start-program-button").insertBefore($(".stop-program-button"));
            } else{
              $(".start-program-button").show();
              $(".start-program-button").prop('disabled', false);
              $(".stop-program-button").hide();
              // make the visible button first in the div so the correct style applies
              $(".start-program-button").insertBefore($(".stop-program-button"));
            }
          }

          // translate the view
          $("body").i18n();

          // using jqueryui tooltips
          $( document ).tooltip();

          // progress indicators should be updated at the end as they are sensitive to the sizes and positions of elements
          self.updateProgressIndicators();

        });

      },
      applyReadMode: function(input) {
        // setting selects in read mode
        $(input).find("select").replaceWith(function() {
          return '<span>' + this.selectedOptions[0].innerHTML + '</span>';
        });
        $(input).find("input").replaceWith(function() {
          return '<span>' + this.value + '</span>';
        });
        $(input).find("textarea").replaceWith(function() {
            return '<span>' + this.value.replace(/(\n)/gm,"<br/>") + '</span>';
        });

        return input;
      },
      updateProgressIndicators: function() {
        var self = this;
        var input = $(".programInput");
        var workspace = $(".editorWorkspace");
        var activeSet = $.map(this.model.get("activeNodes"), function(value,index){return [[index, value]];});
        $(".active-node-indicator").addClass("hidden");

        // mark active nodes
        if (self.model.isWorking() && activeSet.length > 0) {
          activeSet.forEach(function(activeNodes) {
            var activeIndicator = $(input).find("#active-" + activeNodes[0]);
            if (activeIndicator.length > 0) {
              var editorWidth = workspace.width();
              var leftOffset = activeIndicator.offset().left - workspace.position().left;
              $(activeIndicator).width(editorWidth);
              $(activeIndicator).offset({
                left: leftOffset
              });

              activeIndicator.removeClass("hidden");

              // if a parent node of an active node is active, hide its indicator
              if (typeof activeIndicator.attr("parent-node") !== "undefined") {
                input.find("#active-" + activeIndicator.attr("parent-node")).addClass("hidden");
              }
            }
          });
        }

        // updated counters
        var counterSet = $.map(this.model.get("nodesCounter"), function(value,index){return [[index, value]];});
        if(counterSet.length > 0){
          counterSet.forEach(function(nodeCounter) {
            var t = $(input).find("#progress-counter-" + nodeCounter[0]);
            $(input).find("#progress-counter-" + nodeCounter[0]).text(nodeCounter[1]);
          });
        }

        // update true/false nodes
        $(".progress-true-false-indicator").each(function(index) {
          var span = $(this);
          var nodeCounter = self.model.get("nodesCounter");
          if(typeof nodeCounter[span.attr("true-node")] !== "undefined" && typeof nodeCounter[span.attr("false-node")] !== "undefined") {
            if(nodeCounter[span.attr("true-node")] > nodeCounter[span.attr("false-node")]){
              span.text($.i18n.t("debugger.yes"));
              span.addClass("progress-true-indicator");
            } else {
              span.text($.i18n.t("debugger.no"));
              span.addClass("progress-false-indicator");
            }
            span.removeClass("hidden");
          } else if ( typeof nodeCounter[span.attr("true-node")] !== "undefined" && typeof nodeCounter[span.attr("false-node")] === "undefined" ) {
            span.text($.i18n.t("debugger.yes"));
            span.addClass("progress-true-indicator");
            span.removeClass("hidden");
          } else if ( typeof nodeCounter[span.attr("true-node")] === "undefined" && typeof nodeCounter[span.attr("false-node")] !== "undefined" ) {
            span.text($.i18n.t("debugger.no"));
            span.addClass("progress-false-indicator");
            span.removeClass("hidden");
          }
        });

        return input;
      },
      /**
      * Render the editor view
      */
      render: function() {

        var self = this;

        // render the editor with the program
        this.$el.html(this.tplEditor({
          program: this.model
        }));

        if (this.model) {
          // put the name of the place by default in the modal to edit
          if (typeof this.model !== 'undefined') {
            $("#edit-program-name-modal .program-name").val(this.model.get("name"));
          }

          // hide the error message
          $("#edit-program-name-modal .text-error").hide();

          // initialize test button
          if(devices.getCoreClock().get("simulated") == true){
            $("#test-program-button").addClass("hidden");
            $("#stop-testing-button").removeClass("hidden");
          } else {
            $("#test-program-button").removeClass("hidden");
            $("#stop-testing-button").addClass("hidden");
          }

          this.refreshDisplay();

          // fix the programs list size to be able to scroll through it
          this.resize($(".programInput"));

        }

        return this;
      }

    });
    return ProgramReaderView;
  });
