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
        "click button.start-program-button": "onStartProgramButton",
        "click button.stop-program-button": "onStopProgramButton",
        "click button.cancel-edit-program-button": "onCancelEditProgram",
        "click button.launch-edit-program-button": "onLaunchEditProgram",
        "click button.edit-popover-button": "onClickEditProgram",
        "click button.delete-program-button": "onDeleteProgramButton",
        "click button.delete-popover-button": "onClickDeleteProgram",
        "click button.cancel-delete-program-button": "onCancelDeleteProgram",

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

        program.set("runningState", "PROCESSING");
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

        program.set("runningState", "DEPLOYED");
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
        if (this.model.get('runningState') === "WAITING") {
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
          $(".secondary-block-node").addClass("hidden");
          if($(".input-spot").next().find(".btn-and").length > 0 || $(".input-spot").next().find(".btn-then").length > 0){
            $(".input-spot").next()[0].remove();
          }
          $(".input-spot").prev().remove();
          $(".input-spot").remove();

          if($(".programInput").children(".seq-block-node").children().length < 1){
            $(".programInput").children(".separator").addClass("hidden");
            $(".programInput").children(".seq-block-node").addClass("hidden");
          }
          if($(".programInput").children(".set-block-node").children().length < 1){
            $(".programInput").children(".separator").addClass("hidden");
            $(".programInput").children(".set-block-node").addClass("hidden");
          }

          if(typeof this.model !== "undefined"){
            if (this.model.get("runningState") === "PROCESSING" || this.model.get("runningState") === "KEEPING" || this.model.get("runningState") === "WAITING") {
              $("#led-" + this.model.get("id")).addClass("led-yellow").removeClass("led-orange").removeClass("led-default");
              $(".start-program-button").hide();
              $(".stop-program-button").show();
            } else if (this.model.get("runningState") === "INVALID"){
              $("#led-" + this.model.get("id")).addClass("led-orange").removeClass("led-yellow").removeClass("led-default");
              $(".start-program-button").show();
              //$(".start-program-button").hide(); Now we don't hide it just disable it
              $(".start-program-button").prop('disabled', true);
              $(".stop-program-button").hide();
            } else{
              $("#led-" + this.model.get("id")).addClass("led-default").removeClass("led-yellow").removeClass("led-orange");
              $(".start-program-button").show();
              $(".stop-program-button").hide();
            }
          }
          $("body").i18n();

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
        var activeSet = $.map(this.model.get("activeNodes"), function(value,index){return [[index, value]];});

        // mark active nodes as locked
        if(activeSet.length > 0){
          activeSet.forEach(function(activeNodes) {
            if($(input).find("#active-" + activeNodes[0]).length > 0 && activeNodes[1] == true) {
                var workspace = $(".editorWorkspace");
                workspace.children("#active-" + activeNodes[0]).remove();
                var activeIndicator = $(input).find("#active-" + activeNodes[0]);
                var editorWidth = workspace.width();
                $(activeIndicator).width(editorWidth);

                activeIndicator = activeIndicator.detach();
                $(activeIndicator.first()).appendTo(workspace);

                var targetPosition = $("#" + activeIndicator.attr("target-node")).offset();
                if(targetPosition){
                  $(activeIndicator).offset({top:targetPosition.top - workspace.offset().top, left:0});
                }

                $(".editorWorkspace").find("#active-" + activeNodes[0]).removeClass("hidden");

                if(activeIndicator.attr("parent-node") !== null) {
                  $(".editorWorkspace").children("#active-" + activeIndicator.attr("parent-node")).addClass("hidden");
                }
            }
            else if($(input).find("#active-" + activeNodes[0]).length > 0 && activeNodes[1] == false){
              $(".editorWorkspace").children("#active-" + activeNodes[0]).addClass("hidden");
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
          var test =  nodeCounter[span.attr("true-node")];
          var test2 = nodeCounter[span.attr("false-node")];
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
          console.log( index + " : " + span.attr("id") + " true: " + span.attr("true-node") + " false: " + span.attr("false-node"));
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

          this.refreshDisplay();

          // fix the programs list size to be able to scroll through it
          this.resize($(".scrollable"));

          $(".programInput").height("auto");
        }
        return this;
      }

    });
    return ProgramReaderView;
  });
