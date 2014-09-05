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
        "click button.delete-program-button": "onDeleteProgramButton",
        "click button.cancel-edit-program-button": "onCancelEditProgram",
        "click button.launch-edit-program-button": "onLaunchEditProgram",
        "click button.edit-popover-button": "onClickEditProgram",
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
      * Callback when the user has clicked on the button to remove a program. Remove the program
      */
      onDeleteProgramButton: function() {
        // delete the program
        this.model.destroy();

        // navigate to the list of programs
        appRouter.navigate("#programs", {trigger: true});
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
            content: "<div><button type='button' class='btn btn-default cancel-edit-program-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-primary launch-edit-program-button'>" + $.i18n.t("programs.edit-program") + "</button></div>",
            placement: "bottom"
            });
            // listen the hide event to destroy the popup, because create to every click on Edit
            this.$el.find("#edit-program-popover").on('hidden.bs.popover', function () {
                self.onCancelEditProgram();
            })
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
          input = self.updateProgressIndicators(input);
          $(".programInput").html(input).addClass("read-only");
          $(".secondary-block-node").addClass("hidden");
          if($(".input-spot").next().find(".btn-and").length > 0 || $(".input-spot").next().find(".btn-then").length > 0){
            $(".input-spot").next()[0].remove();
          }
          $(".input-spot").prev().remove();
          $(".input-spot").remove();
        });
        if(typeof this.model !== "undefined"){
          if (this.model.get("runningState") === "PROCESSING" || this.model.get("runningState") === "KEEPING" || this.model.get("runningState") === "WAITING") {
            $("#led-" + this.model.get("id")).addClass("led-yellow").removeClass("led-orange").removeClass("led-default");
            $(".start-program-button").hide();
            $(".stop-program-button").show();
          } else if (this.model.get("runningState") === "INVALID"){
            $("#led-" + this.model.get("id")).addClass("led-orange").removeClass("led-yellow").removeClass("led-default");
            $(".start-program-button").hide();
            $(".stop-program-button").hide();
          } else{
            $("#led-" + this.model.get("id")).addClass("led-default").removeClass("led-yellow").removeClass("led-orange");
            $(".start-program-button").show();
            $(".stop-program-button").hide();
          }
        }
        $("body").i18n();
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
      updateProgressIndicators: function(input) {
        var activeSet = $.map(this.model.get("activeNodes"), function(value,index){return [[index, value]];});

        // mark active nodes as locked
        if(activeSet.length > 0){
          activeSet.forEach(function(activeNodes) {
            var t = $(input).find("#progress-" + activeNodes[0]);
            if(activeNodes[1] == true) {
              $(t.find(".locked-node-indicator")[0]).addClass("hidden");
              $(t.find(".unlocked-node-indicator")[0]).removeClass("hidden");
            }
            else{
              $(t.find(".unlocked-node-indicator")[0]).addClass("hidden");
              $(t.find(".locked-node-indicator")[0]).removeClass("hidden");
            }
          });
        }
        if(this.model.get("runningState") === "DEPLOYED" || this.model.get("runningState") === "INVALID"){
          $(input).find(".unlocked-node-indicator").addClass("hidden");
          $(input).find(".locked-node-indicator").addClass("hidden");
        }

        // updated counters
        var counterSet = $.map(this.model.get("nodesCounter"), function(value,index){return [[index, value]];});
        if(counterSet.length > 0){
          counterSet.forEach(function(nodeCounter) {
            var t = $(input).find("#progress-counter-" + nodeCounter[0]);
            $(input).find("#progress-counter-" + nodeCounter[0]).text(nodeCounter[1]);
          });
        }

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
          // initialize the popover
          this.$el.find("#delete-popover").popover({
            html: true,
            content: "<button type='button' class='btn btn-danger delete-program-button'>" + $.i18n.t("form.delete-button") + "</button>",
            placement: "bottom"
          });

          // put the name of the place by default in the modal to edit
          if (typeof this.model !== 'undefined') {
            $("#edit-program-name-modal .program-name").val(this.model.get("name"));
          }

          // hide the error message
          $("#edit-program-name-modal .text-error").hide();

          this.refreshDisplay();

          // fix the programs list size to be able to scroll through it
          this.resizeDiv($(self.$el.find(".editorWorkspace")[0]), true);

          $(".programInput").height("auto");
        }
        return this;
      }

    });
    return ProgramReaderView;
  });
