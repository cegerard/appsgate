define([
    "app",
    "text!templates/program/menu/menu.html",
    "text!templates/program/menu/programContainer.html",
    "text!templates/program/menu/addbutton.html"
], function(App, programMenuTemplate, programContainerMenuTemplate, addProgramButtonTemplate) {

    var ProgramMenuView = {};
    /**
     * Render the side menu for the programs
     */
    ProgramMenuView = Backbone.View.extend({
        tpl: _.template(programMenuTemplate),
        tplProgramContainer: _.template(programContainerMenuTemplate),
        tplAddProgramButton: _.template(addProgramButtonTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click a.list-group-item": "updateSideMenu",
            "shown.bs.modal #add-program-modal": "initializeModal",
            "hide.bs.modal #add-program-modal": "toggleModalValue",
            "click #add-program-modal button.valid-button": "validAddProgram",
            "keyup #add-program-modal input:text": "validAddProgram",
            "click .deactivate-all-programs-button": "onStopAllPrograms"
        },
		/**
		* Attributes to know the type of this menu
		*/
		attributes: {
			"class": "ProgramMenuView"
		},
        /**
         * Listen to the programs collection updates and refresh if any
         *
         * @constructor
         */
        initialize: function() {
            this.listenTo(programs, "add", this.render);
            this.listenTo(programs, "remove", this.render);
            this.listenTo(programs, "change", this.autoupdate);
        },
        autoupdate: function(model) {
          this.$el.find("#side-" + model.get("id")).replaceWith(this.tplProgramContainer({
              program: model,
              active: Backbone.history.fragment.split("/programs")[1] === model.get("name") ? true : false
          }));

          this.updateSideMenu();

          $(".deactivate-all-programs-button").prop("disabled",programs.allProgramsStopped());

          // translate the view
          this.$el.i18n();
        },
        /**
         * Update the side menu to set the correct active element
         *
         * @param e JS click event
         */
        updateSideMenu: function(e) {
            _.forEach($("a.list-group-item"), function(item) {
                $(item).removeClass("active");
            });

            if (typeof e !== "undefined") {
                $(e.currentTarget).addClass("active");
            } else if(Backbone.history.fragment.split("/")[1] !== ""){
                $("#side-" + Backbone.history.fragment.split("/")[1]).addClass("active");
            } else {
                this.$el.find(".list-group .list-group-item:first").addClass("active");
            }
        },
        /**
         * Clear the input text, hide the error message, check the checkbox and disable the valid button by default
         */
        initializeModal: function() {
            $("#add-program-modal input").val("");
            $("#add-program-modal input").focus();
            $("#add-program-modal .text-danger").addClass("hide");
            $("#add-program-modal input:checkbox").prop("checked", true);
            $("#add-program-modal .valid-button").addClass("disabled");
            $("#add-program-modal .valid-button").addClass("valid-disabled");

            // tell the router that there is a modal
            appRouter.isModalShown = true;
        },
        /**
         * Tell the router there is no modal anymore
         */
        toggleModalValue: function() {
            appRouter.isModalShown = false;
        },
        /**
         * Check the current value of the input text and show an error message if needed and activate or disactivate the valid button
         *
         * @return false if the typed name already exists, true otherwise
         */
        checkProgramName: function() {
            // Check the length of the name
            if ($("#add-program-modal input:text").val().length > App.MAX_NAME_LENGTH) {
                $("#add-program-modal .text-danger")
                        .text($.i18n.t("modal-add-program.name-empty"))
                        .removeClass("hide");
                $("#add-program-modal .valid-button").addClass("disabled");
                $("#add-program-modal .valid-button").addClass("valid-disabled");

                return false;
            }
            if ($("#add-program-modal input:text").val() === "") {
                $("#add-program-modal .text-danger")
                        .text($.i18n.t("modal-add-program.name-empty"))
                        .removeClass("hide");
                $("#add-program-modal .valid-button").addClass("disabled");
                $("#add-program-modal .valid-button").addClass("valid-disabled");

                return false;
            }
            // name contains html code
            if (/(&|>|<)/.test($("#add-program-modal input:text").val())) {
                $("#add-program-modal .text-danger")
                        .text($.i18n.t("edit-name-modal.contains-html"))
                        .removeClass("hide");
                $("#add-program-modal .valid-button").addClass("disabled");
                $("#add-program-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // name already exists
            if (programs.where({name: $("#add-program-modal input:text").val()}).length > 0) {
                $("#add-program-modal .text-danger")
                        .text($.i18n.t("modal-add-program.name-already-existing"))
                        .removeClass("hide");
                $("#add-program-modal .valid-button").addClass("disabled");
                $("#add-program-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // ok
            $("#add-program-modal .text-danger").addClass("hide");
            $("#add-program-modal .valid-button").removeClass("disabled");
            $("#add-program-modal .valid-button").removeClass("valid-disabled");

            return true;
        },
        /**
         * Check if the name of the program does not already exist. If not, create the program
         * Hide the modal when done
         *
         * @param e JS event
         */
        validAddProgram: function(e) {
            var self = this;
            if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
                // create the program if the name is ok
                if (this.checkProgramName()) {
                    var program;


                    // instantiate a model for the new program
                    program = programs.create({
                        name: $("#add-program-modal input:text").val(),
                        daemon: "false",
                        isNew: "true"
                    });

                    // hide the modal
                    $("#add-program-modal").modal("hide");

                    // instantiate the program and add it to the collection after the modal has been hidden
                    $("#add-program-modal").on("hidden.bs.modal", function() {
                        // tell the router there is no modal any more
                        appRouter.isModalShown = false;

                        // send the program to the backend
                        program.save();

                        // display the new program
                        appRouter.programsRouter.editor(program.get("id"));
                    });
                }
            } else if (e.type === "keyup") {
                this.checkProgramName();
            }
        },
        isProgramEmpty:function(program){
            if (program.get("body").rules === undefined) {
                return true;
            }
          return (program.get("body").rules.length === 1 && program.get("body").rules[0].type === "empty");
        },
        onStopAllPrograms:function() {
          programs.stopAllPrograms();
        },
        /**
         * Render the side menu
         */
        render: function() {
            if (!appRouter.isModalShown) {
                var self = this;

                // we build a temporary container with each model
                var container = document.createDocumentFragment();

                // initialize the content
                $(container).html(this.tpl());

                $(container).append("<button type='button' class='btn btn-info deactivate-all-programs-button'><span data-i18n='programs-menu.deactivate-all-programs'></span></div><br>");


                // "add program" button to the side menu
                $(container).append(this.tplAddProgramButton());

                // for each program, add a menu item
                $(container).append(this.tpl());
                var programsDiv = $(container).children(".list-group");

                programs.forEach(function(program) {
                    // hack to make empty programs to appear as invalid
                    if(self.isProgramEmpty(program)){
                      program.set("runningState", "INVALID");
                    }

                    programsDiv.append(self.tplProgramContainer({
                        program: program,
                        active: Backbone.history.fragment.split("/programs")[1] === program.get("name") ? true : false
                    }));
                });

                programsDiv.addClass("scrollable-menu");

                // we add all elements all at once to avoid rendering them individually and thus reflowing the dom several times
                this.$el.html(container);

                // translate the view
                this.$el.i18n();

                this.$el.find(".deactivate-all-programs-button").prop("disabled",programs.allProgramsStopped());

                // fix the programs list size to be able to scroll through it
                this.resize(self.$el.find(".scrollable-menu"));

                return this;
            }
        }

    });
    return ProgramMenuView;
});
