define([
  "app",
  "views/place/menu",
  "views/place/details"
  ], function (App, PlaceMenuView, PlaceDetailsView) {

	var PlaceRouter = {};
	// router
	PlaceRouter = Backbone.Router.extend({
		routes: {
			"places": "list",
			"places/:id": "details"
		},
		// list all the places
		list: function () {
			// display the side menu
			appRouter.showMenuView(new PlaceMenuView());

			// set active the first element - displayed by default
			$($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

			// display the first places
			var placeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("place-")[1];
			if (placeId) {

				// update the url
				appRouter.navigate("#places/" + placeId, {
					replace: true
				});

				$(".nav-item").removeClass("active");
				$(".places-nav").addClass("active");

				appRouter.showDetailsView(new PlaceDetailsView({
					model: places.get(placeId)
				}));
			}

			dispatcher.trigger("router:loaded");
		},
		// show the details of a places (i.e. list of devices in this place)
		details: function (id) {
			// Direct access device, , need to add the menu
			if (appRouter.currentMenuView === null || appRouter.currentMenuView.attributes === undefined || appRouter.currentMenuView.attributes.class !== "PlaceMenuView") {
				// display the side menu
				appRouter.showMenuView(new PlaceMenuView());
				appRouter.currentMenuView.updateSideMenu();
				// update tab
				$(".nav-item").removeClass("active");
				$(".places-nav").addClass("active");
			}

			appRouter.showDetailsView(new PlaceDetailsView({
				model: places.get(id)
			}));
		}
	});
	return PlaceRouter;
});