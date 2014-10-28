define([
    "app",
    "text!templates/home/home.html",
    "text!templates/home/navbar.html",
    "text!templates/home/circlemenu.html",
    "text!templates/universes/universeContainer.html",
    "text!templates/universes/userUniverseContainer.html"
], function(App, homeTemplate, navbarTemplate, circleMenuTemplate, universeTemplate, userUniverseTemplate) {

    var HomeView = {};

    /**
     * View used as home page of the application
     */
    HomeView = Backbone.View.extend({
        el: $("#main"),
        template: _.template(homeTemplate),
        tplNavbar: _.template(navbarTemplate),
        tplCircleMenu: _.template(circleMenuTemplate),
        tplUniverseContainer: _.template(universeTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click div.fundamental-universe": "navigateIn",
        },
        /**
         * constructor
         */
        initialize: function() {
            HomeView.__super__.initialize.apply(this, arguments);

            dispatcher.on("dataReady", function() {
              $(".disabled").switchClass("disabled","",{"duration":1000});
              $(".loading-widget").animate({opacity: 0}, 1000);
            });

        },
        navigateIn: function(e) {
            if (!this.zooming && !this.editMode) {
                this.zooming = true;

                var node = $("#main");
                var divHeight = window.innerHeight - (node.offset().top + node.outerHeight(true) - node.innerHeight());
                var divWidth = window.innerWidth - (node.offset().left + node.outerWidth(true) - node.innerWidth());

                var target = e.target;
                while (target && !target.classList.contains('fundamental-universe')) {
                    target = target.parentNode;
                }
                target = $(target);

                if(!target.hasClass("disabled")){
                    var targetWidth = target.innerWidth();
                    var targetHeight = target.innerHeight();
                    var targetX = target.offset().left;
                    var targetY = target.offset().top;

                    var zoomingTarget = target.clone();
                    target.css("opacity", "0");

                    zoomingTarget.css("position", "absolute");
                    zoomingTarget.offset({left: targetX, top: targetY});
                    zoomingTarget.width(targetWidth);
                    zoomingTarget.height(targetHeight);
                    zoomingTarget.css("z-index", 1000);
                    zoomingTarget.appendTo(node);

                    var cb = function() {
                        this.zooming = false;
                        if (target.hasClass("fundamental-universe") ) {
                            appRouter.navigate(target.attr("id"), {trigger: true});
                        }
                    };

                    zoomingTarget.animate({top: node.offset().top - 50-divHeight/4, left: node.offset().left-divWidth/4, width: divWidth*1.5, height: divHeight*1.5, opacity: 0}, 1000, cb);
                    $("#universeList").animate({opacity: 0}, 500);
                }
                else {
                    this.zooming = false;
                }
            }
        },
        // render the homepage of the application
        render: function() {
            var self = this;
            this.$el.append(this.template());

            // translate the view
            $("body").i18n();

            return this;
        }
    });

    return HomeView;
});
