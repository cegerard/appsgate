define([
    "app",
    "text!templates/program/menu/menu.html",
    "text!templates/program/menu/programContainer.html",
    "text!templates/program/menu/addbutton.html",
    "text!templates/devices/menu/coreClockContainer.html"
], function(App, programMenuTemplate, programContainerMenuTemplate, addProgramButtonTemplate, coreClockContainerMenuTemplate) {

    var ProgramMenuView = {};
    /**
     * Render the side menu for the programs
     */
    ProgramMenuView = Backbone.View.extend({
        tpl: _.template(programMenuTemplate),
        tplProgramContainer: _.template(programContainerMenuTemplate),
        tplAddProgramButton: _.template(addProgramButtonTemplate),
        tplCoreClockContainer: _.template(coreClockContainerMenuTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click a.list-group-item": "updateSideMenu",
            "shown.bs.modal #add-program-modal": "initializeModal",
            "hidden.bs.modal #add-program-modal": "toggleModalValue",
            "click #add-program-modal button.valid-button": "validAddProgram",
            "keyup #add-program-modal input:text": "validAddProgram"
        },
        /**
         * Listen to the programs collection updates and refresh if any
         *
         * @constructor
         */
        initialize: function() {
            this.listenTo(programs, "add", this.render);
            this.listenTo(programs, "remove", this.render);
            this.listenTo(programs, "change", this.render);
            this.listenTo(devices.getCoreClock(), "change", this.refreshClockDisplay);
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
            } else {
                $("#side-" + Backbone.history.fragment.split("/")[1]).addClass("active");
            }
        },
        /**
         * Refreshes the time display without rerendering the whole screen
         */
        refreshClockDisplay: function() {

            //remove existing node
            $(this.$el.find(".list-group")[0]).children().remove();

            //refresh the clock
            $(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
                device: devices.getCoreClock(),
                active: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
            }));

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
            // name is empty
            if ($("#add-program-modal input:text").val() === "") {
                $("#add-program-modal .text-danger")
                        .text($.i18n.t("modal-add-program.name-empty"))
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
        /**
         * Render the side menu
         */
        render: function() {
            if (!appRouter.isModalShown) {
                var self = this;

                // initialize the content
                this.$el.html(this.tpl());

                // put the time on the top of the menu
                $(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
                    device: devices.getCoreClock(),
                    active: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
                }));

                // "add program" button to the side menu
                this.$el.append(this.tplAddProgramButton());

                // for each program, add a menu item
                this.$el.append(this.tpl());
                var programsDiv = $(self.$el.find(".list-group")[1]);

                // we build a temporary container with each model
                var container = document.createDocumentFragment();
                programs.forEach(function(program) {
                    // hack to make empty programs to appear as invalid
                    if(self.isProgramEmpty(program)){
                      program.set("runningState", "INVALID");
                    }

                    $(container).append(self.tplProgramContainer({
                        program: program,
                        active: Backbone.history.fragment.split("/programs")[1] === program.get("name") ? true : false
                    }));
                });

                programsDiv.addClass("scrollable-menu");

                // we add all elements all at once to avoid rendering them individually and thus reflowing the dom several times
                programsDiv.append(container);

                // set active the current menu item
                this.updateSideMenu();

                // translate the view
                this.$el.i18n();

                // fix the programs list size to be able to scroll through it
                this.resize(self.$el.find(".scrollable-menu"));

                return this;
            }
        }

    });
    return ProgramMenuView;
});
