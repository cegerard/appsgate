define([
    "app",
    "text!templates/dependancy/graph.html",
	"text!templates/home/loadingWidget.html"
], function (App, GraphTemplate, LoadingTemplate) {

	var GraphView = {};

	GraphView = Backbone.View.extend({
		loadingTemplate: _.template(LoadingTemplate),
		template: _.template(GraphTemplate),

		events: {
			"click button.refresh-button": "onRefreshButton",
			"click button.unfix-button": "onUnfixButton",
			"click button.search-button": "onSearchButton",
			"keyup .search-input-text": "onSearchButton"
		},

		initialize: function () {
			var self = this;

			// Listen the change done when check filter all
			this.model.on("change:currentEntitiesTypes", function () {
				force.stop();
				self.model.updateEntitiesShown();
				self.update(self.model);
				force.start();
			});

			// Listen the change done when check filter all
			this.model.on("change:currentRelationsTypes", function () {
				force.stop();
				self.model.updateRelationsShown();
				self.update(self.model);
				force.start();
			});

			// Update the graph when the modifications need to reload
			self.listenTo(dispatcher, "UpdateGraph", function (args) {
				console.log("show");
				$("#graph>.loading-widget").show();
				dependancies.updateDependency(args.buildGraph);
			});

			// Listener to the end of an update. Hide the loading widget and update the graph and restart the force
			self.listenTo(dispatcher, "UpdateGraphFinished", function () {
				dependancies.isUpdating = false;

				console.log("hide");
				$("#graph>.loading-widget").hide();
				force.stop();
				self.update(self.model);
				force.start();
			});

			// Zoom variables
			savedScale = 1;
			savedTranslate = [0, 0];
			onMouseDownNode = false;
			// Used to hide the popups relations
			onCircleRelation = false;

			ENTITY_WIDTH = 24;
			ENTITY_FOCUS_WIDTH = ENTITY_WIDTH * 1.5;
		},

		onRefreshButton: function () {
			// Notify collection we want the new data
			dependancies.refresh();
			// Reload this page to refresh data
			appRouter.dependancies();
		},

		onSearchButton: function (e) {
			e.preventDefault();

			var nameSearched = $(".search-input-text").val();
			var nodesFound = [];

			// Comparing the string entered to the name of the entities
			force.nodes().forEach(function (d) {
				if (d.name.toLowerCase().indexOf(nameSearched.toLowerCase()) >= 0) {
					nodesFound.push(d);
				}
			});

			if (nodesFound.length === 0 || nodesFound.length === force.nodes().length) {
				nodeEntity.classed("neighborNodeOver", false);
				$($(".search-button")[0]).removeClass("btn-success");
				$(".search-button").prop('disabled', true);
			} else {
				// If there is node containing the string searched, highlight them
				nodeEntity.classed("neighborNodeOver", function (d) {
					return nodesFound.indexOf(d) !== -1;
				});

				// There is only one result, typing enter select it
				if (nodesFound.length === 1) {
					$(".search-button").prop('disabled', false);
					$($(".search-button")[0]).addClass("btn-success");
					if ((e.type === "keyup" && e.keyCode === 13) || (e.type === "click" && e.target.className === "btn btn-default search-button btn-success")) {
						force.stop();
						this.selectAndMoveRootNode(nodesFound[0]);
						$(".search-input-text").select();
						nodeEntity.classed("neighborNodeOver", false);
						force.start();
					}
				} else {
					$($(".search-button")[0]).removeClass("btn-success");
					$(".search-button").prop('disabled', true);
				}
			}
		},

		onUnfixButton: function (e) {
			var self = this;
			force.nodes().forEach(function (d) {
				if (self.model.get("rootNode") !== d) {
					d.fixed = false;
				}
			});
			if (force.alpha() === 0) {
				force.start();
			}
		},

		render: function () {
			this.$el.html(this.template({
				dependancy: this.model
			}));

			// Pour le moment, largeur en fonction de ce qu'on a sur l'ecran et hauteur en dur dans le modèle
			var width = $(".body-content").width(),
				height = this.model.get("height");

			this.model.set({
				width: width,
				height: height
			});


			this.createFilters(this.model);
			this.updateCheckAllEntities();
			this.updateCheckAllRelations();
			// update model, if we have an entity disable by default (ie selector)
			this.model.updateEntitiesShown();

			// Zoom d3 object
			pan = d3.behavior.zoom()
				.on("zoom", rescale)
				.on("zoomstart", function () {
					// When pan/zoom change cursor if no on mouse
					if (!onMouseDownNode) {
						$("body").css("cursor", "move");
					}
					// Temporaire : Hide all popovers if pan ...
					if (!onCircleRelation) {
						$('.popover').each(function (pop) {
							if ($(this).is(":visible")) {
								$(this).popover('hide');
							}
						});
					}
				})
				.on("zoomend", function () {
					$("body").css("cursor", "default");
				});

			// Add the svg to html
			svg = d3.select("#graph").select("svg")
				.attr("width", width)
				.attr("height", height)
				.call(pan)
				.on("dblclick.zoom", null)
				.append("g");

			// Add the div for links & nodes
			svg.append("svg:g").attr("id", "groupLink");
			svg.append("svg:g").attr("id", "groupNode");

			// Build the directional arrows for the links/edges
			// Per-type markers, as they don't inherit styles.
			svg.append("svg:defs").selectAll("marker")
				.data(["targetingFocus", "targetingRefFocus", "targeting", "reference", "isLocatedIn", "isPlanified", "denotes"])
				.enter().append("svg:marker")
				.attr("id", String)
				.attr("viewBox", "0 -5 10 10")
				.attr("refX", function (t) {
					if (t === "targetingFocus" || t === "targetingRefFocus") {
						return 38;
					} else {
						return 28;
					}
				})
				.attr("markerWidth", 6)
				.attr("markerHeight", 6)
				.attr("orient", "auto")
				.append("svg:path")
				.attr("d", "M0,-5L10,0L0,5");

			// Creation of Force
			force = d3.layout.force()
				.size([width, height])
				.gravity(0.03)
				.on("tick", this.tick.bind(this));

			// Test if we open graph from an entity, if yes, focus it
			if (this.model.get("rootNode") !== "") {
				this.selectAndMoveRootNode(this.model.get("rootNode"));
			}

			// Call update to update the state of the force (node/link)
			this.update(this.model);

			// Initialize all the popover one time
			$('[data-toggle="popover"]').popover();

			force.start();

			// translate the view
			this.$el.i18n();

			this.resize($(".scrollable"));
		},

		update: function (model) {
			var self = this;

			force.nodes(model.get("currentEntities"));
			force.links(model.get("currentRelations"));

			/******* NODES (=Entities) *******/

			// Bind node to the currentEntities, a node is represented by her ID 
			nodeEntity = svg.select("#groupNode").selectAll(".nodeGroup").data(force.nodes(), function (d) {
				return d.id;
			});

			// Text node modified - TODO : check 
			var nModified = svg.select("#groupNode").selectAll(".nodeGroup").select("text").data(model.get("currentEntities"), function (d) {
				return d.id;
			});
			nModified.text(function (d) {
				if (d.type === "selector") {
					return $.i18n.t("dependancy.type.entity.selector.type-" + d.name);
				} else if (d.type === "time") {
					return $.i18n.t("dependancy.type.entity.time." + d.name);
				} else {
					return d.name;
				}
			});

			// Update: selection on all the element
			nodeEntity.each(function (a) {
				// When an entity become "ghost", append line and remove decoration (circle or square)
				if (a.isGhost && d3.select(this).selectAll("line").empty()) {
					d3.select(this).append("line")
						.attr("class", "ghost-decoration")
						.attr("opacity", 1)
						.attr("x1", -9)
						.attr("y1", -9)
						.attr("x2", 9)
						.attr("y2", 9);

					d3.select(this).append("line")
						.attr("class", "ghost-decoration")
						.attr("opacity", 1)
						.attr("x1", -9)
						.attr("y1", 9)
						.attr("x2", 9)
						.attr("y2", -9);

					// Remove program decoration 
					var programDecoration = d3.select(this).selectAll(".shape-program");
					if (!programDecoration.empty()) {
						programDecoration.remove();
					}

					// Remove device decoration 
					var deviceDecoration = d3.select(this).selectAll(".circle-device-state");
					if (!deviceDecoration.empty()) {
						deviceDecoration.remove();
					}

				} else if (!a.isGhost && !d3.select(this).selectAll("line").empty()) {
					// Entity reappeared
					// Remove ghost decoration
					d3.select(this).selectAll("line").remove();

					// Readd the decorations
					// shape STATUS PROGRAMS
					// Type program, add form to indicate running or not. 
					if (a.type === "program") {
						if (a.state === "DEPLOYED" || a.state === "INVALID" || a.state === "INCOMPLETE") {
							d3.select(this).append("svg:path")
								.attr("class", "shape-program")
								.attr("d", "M0,-7L0,7L14,7L14,-7L0,-7")
								.attr('opacity', 1);
						} else if (a.state === "PROCESSING" || a.state === "LIMPING") {
							d3.select(this).append("svg:path")
								.attr("class", "shape-program")
								.attr("d", "M0,-7L14,0L0,7L0,-7")
								.attr('opacity', 1);
						}

					}

					// shape STATUS DEVICES
					if (a.type === "device" && a.deviceState !== undefined) {
						d3.select(this).append("circle")
							.attr("class", "circle-device-state")
							.attr("opacity", 1)
							.attr("r", 7);
					}
				}
			});

			// New nodes
			var nEnter = nodeEntity.enter().append("svg:g")
				.attr("class", "nodeGroup")
				.call(force.drag)
				.on("dblclick", this.dblclick.bind(this))
				.on("mouseover", function (d) {
					nodeEntity.classed("nodeOver", function (d2) {
						return d2 === d;
					});
					nodeEntity.classed("neighborNodeOver", function (d2) {
						return model.neighboring(d, d2);
					});
				})
				.on("mouseout", function (d) {
					nodeEntity.classed("nodeOver", false);
					nodeEntity.classed("neighborNodeOver", false);
					nodeEntity.classed("fixedNode", function (d) {
						//Avoid to have a fixedNode class when mouseover until the end of force and no click
						return d.fixed && d !== model.get("rootNode");
					});
				})
				.on("mousedown", mousedown)
				.on("mouseup", function (d) {
					mouseup(d, this, model.get("rootNode"));
				})
				.each(function (a) {
					// CIRCLE
					d3.select(this).append("circle")
						.attr("class", "circleNode")
						.attr("cx", function (m) {
							return 0;
						})
						.attr("cy", function (m) {
							return 0;
						})
						.attr("r", 0);
					// IMAGE
					d3.select(this).append("image")
						.attr("xlink:href", function (d) {
							var imgNode = "";
							if (d.type === "device") {
								imgNode = "app/img/home/device3.svg";
							} else if (d.type === "time") {
								imgNode = "app/img/home/calendar.svg";
							} else if (d.type === "place") {
								imgNode = "app/img/home/place1.svg";
							} else if (d.type === "program") {
								imgNode = "app/img/home/program2.svg";
							} else if (d.type === "service") {
								imgNode = "app/img/home/service1.svg";
							} else if (d.type === "selector") {
								imgNode = "app/img/home/devices-selector.svg";
							}
							return imgNode;
						})
						.attr('opacity', 0)
						.attr('x', -12)
						.attr('y', -12)
						.attr('width', ENTITY_WIDTH)
						.attr('height', ENTITY_WIDTH);
					// TEXT
					d3.select(this).append("text")
						.attr("class", "label-name")
						.attr("opacity", 0)
						.text(function (d) {
							if (d.type === "selector") {
								return $.i18n.t("dependancy.type.entity.selector.type-" + d.name);
							} else if (d.type === "time") {
								return $.i18n.t("dependancy.type.entity.time." + d.name);
							} else {
								return d.name;
							}
						})

					// shape STATUS PROGRAMS
					// Type program, add form to indicate running or not. 
					if (a.type === "program") {
						if (a.state === "DEPLOYED" || a.state === "INVALID" || a.state === "INCOMPLETE") {
							d3.select(this).append("svg:path")
								.attr("class", "shape-program")
								.attr("d", "M0,-7L0,7L14,7L14,-7L0,-7")
								.attr('opacity', 0);
						} else if (a.state === "PROCESSING" || a.state === "LIMPING") {
							d3.select(this).append("svg:path")
								.attr("class", "shape-program")
								.attr("d", "M0,-7L14,0L0,7L0,-7")
								.attr('opacity', 0);
						}

					}

					// shape STATUS DEVICES
					if (a.type === "device" && a.deviceState !== undefined) {
						d3.select(this).append("circle")
							.attr("class", "circle-device-state")
							.attr("opacity", 0)
							.attr("r", 7);
					}

					// shape GHOST
					if (a.isGhost) {
						d3.select(this).append("line")
							.attr("class", "ghost-decoration")
							.attr("opacity", 0)
							.attr("x1", -9)
							.attr("y1", -9)
							.attr("x2", 9)
							.attr("y2", 9);

						d3.select(this).append("line")
							.attr("class", "ghost-decoration")
							.attr("opacity", 0)
							.attr("x1", -9)
							.attr("y1", 9)
							.attr("x2", 9)
							.attr("y2", -9);
					}
				});

			nEnter.select("circle").transition().duration(800).attr("r", 14);
			nEnter.select("image").transition().duration(1000).style("opacity", 1);
			nEnter.select(".shape-program").transition().duration(800).style("opacity", 1);
			nEnter.select(".circle-device-state").transition().duration(800).style("opacity", 1);
			nEnter.select("text").transition().duration(800).style("opacity", 1);
			nEnter.selectAll(".ghost-decoration").transition().duration(800).style("opacity", 1);

			nodeEntity.exit().select("image").transition().duration(600).style("opacity", 0);
			nodeEntity.exit().select("text").transition().duration(700).style("opacity", 0);
			nodeEntity.exit().select("circle").transition().duration(700).attr("r", 0);
			nodeEntity.exit().select(".shape-program").transition().duration(700).style("opacity", 0);
			nodeEntity.exit().select(".circle-device-state").transition().duration(700).style("opacity", 0);
			nodeEntity.exit().selectAll(".ghost-decoration").transition().duration(700).style("opacity", 0);
			nodeEntity.exit().transition().duration(800).remove();



			/******* LINKS (=Relations) *******/

			pathLink = svg.select("#groupLink").selectAll(".linkGroup")
				.data(force.links(), function (d) {
					return d.source.id + "-" + d.target.id;
				});

			pathLink
				.enter().append("svg:g")
				.attr("class", "linkGroup")
				.each(function (l, i) {
					// PATH RELATION
					d3.select(this).append("svg:path")
						.attr("class", function (d) {
							return "link " + d.type;
						})
						.attr("marker-end", function (d) {
							return "url(#" + d.type + ")";
						})

					.attr("refX", -30);
					// PATH TEXT
					d3.select(this).append("svg:path")
						.attr("class", function (d) {
							return "linkText";
						})
						.attr("id", function (d) {
							return "linkID_" + i;
						});
					// CIRCLE
					d3.select(this).append("circle")
						.attr("class", "circle-information")
						.attr("r", 2)
						.attr("fill", "red")
						.attr("data-container", "#graph")
						.attr("data-toggle", "popover")
						.attr("data-content", function (d) {
							if (d.referenceData !== undefined) {
								var referenceString = "";
								d.referenceData.forEach(function (ref) {
									if (ref.referenceType !== undefined && ref.method !== undefined) {
										referenceString += ref.referenceType + " : " + ref.method + "<br />";
									}
								});
								return referenceString;
							} else {
								return null;
							}
						})
						.attr("data-title", function (d) {
							return $.i18n.t("dependancy.type.relation." + d.type);
						})
						.attr("data-template", function (d) {
							if (d.referenceData) {
								return '<div class="popover" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>';
							} else {
								return '<div class="popover" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3></div>';
							}
						})
						.attr("data-html", true)
						.on("mouseover", function (d) {
							onCircleRelation = true;
						})
						.on("mouseout", function (d) {
							onCircleRelation = false;
						})
						.on("click", function (d)  {
							$((d3.select(this))[0]).popover();
						});
				});

			pathLink.exit().remove();

			/******* FORCE *******/

			force
				.linkDistance(function (d) {
					var distanceSource = model.getLinkDistance(d.source);
					var distanceTarget = model.getLinkDistance(d.target);
					return Math.max(distanceSource, distanceTarget);
				})
				.linkStrength(function (l) {
					// Liens autour du root plus 'forts' que les autres plus relachés..
					if (l.source === model.get("rootNode") || l.target === model.get("rootNode")) {
						return 0.8;
					} else {
						return 0.3;
					}
				})
				.charge(function (d) {
					if (d === model.get("rootNode")) {
						return -550;
					} else {
						return -180;
					}
				});

		},


		tick: function (e) {
			var self = this;

			nodeEntity
				.attr("transform", function (d) {
					var transf = "";
					transf += "translate(" + (d.x) + "," + (d.y) + ")";
					if (d === self.model.get("rootNode")) {
						transf += "scale(1.5)";
					} else {
						transf += "scale(1)";
					}
					return transf;
				})
				.classed("node-0", function (d) {
					return d === self.model.get("rootNode");
				})
				.classed("node-1", function (d) {
					return self.model.neighboring(d, self.model.get("rootNode"));
				})
				.classed("node-2", function (d) {
					return self.model.getDepthNeighbor(d) === 2;
				})
				.classed("node-more", function (d) {
					// Transparence nodes if no depth 0/1/2 and if there is a focus
					return self.model.get("rootNode") !== "" && (self.model.getDepthNeighbor(d) > 2 || self.model.getDepthNeighbor(d) === -1);
				})
				.classed("fixedNode", function (d) {
					return d !== self.model.get("rootNode") && d.fixed && !$(this).hasClass("nodeOver");
				})
				.classed("visible", function (d) {
					var maxR = ENTITY_FOCUS_WIDTH / 2;
					return (0 < d.x + maxR) && (d.x - maxR < self.model.get("width")) && (0 < d.y + maxR) && (d.y - maxR < self.model.get("height"));
				});

			nodeEntity.selectAll("circle")
				.classed("program-multiple-writing-reference", function (d) {
					return !d.isGhost && self.model.isMultipleTargeted(d);
				})
				.classed("ghost-decoration", function (d) {
					return d.isGhost;
				})

			nodeEntity.selectAll(".circle-device-state")
				.attr("transform", function (d) {
					return "translate(5,10)";
				})
				.classed("circle-device-state-true", function (d) {
					if (d.deviceType && d.deviceType === "3") {
						return (d.deviceState === false || d.deviceState === "false");
					} else {
						return (d.deviceState === true || d.deviceState === "true");
					}
				})
				.classed("circle-device-state-false", function (d) {
					if (d.deviceType && d.deviceType === "3") {
						return (d.deviceState === true || d.deviceState === "true");
					} else {
						return (d.deviceState === false || d.deviceState === "false");
					}
				});

			nodeEntity.selectAll("text")
				.attr("transform", function (d) {
					if (d === self.model.get("rootNode")) {
						return "translate(0,-18)";
					} else {
						return "translate(0,-15)";
					}
				});

			nodeEntity.selectAll(".shape-program")
				.attr("transform", function (d) {
					return "translate(3,10)";
				})
				.attr("d", function (d) {
					if (d.state === "PROCESSING" || d.state === "LIMPING") {
						return "M0,-7L14,0L0,7L0,-7";
					} else {
						return "M0,-7L0,7L14,7L14,-7L0,-7";
					}
				})
				.classed("program-invalid", function (d) {
					return d.state === "INVALID";
				})
				.classed("program-processing", function (d) {
					return d.state === "PROCESSING";
				})
				.classed("program-deployed", function (d) {
					return d.state === "DEPLOYED";
				})
				.classed("program-limping", function (d) {
					return d.state === "LIMPING";
				})
				.classed("program-incomplete", function (d) {
					return d.state === "INCOMPLETE";
				});


			pathLink.select(".link")
				.attr("d", function (d) {
					// don't look about the orientation, this is the link showed
					return arcPath(false, d);
				})
				.classed("node-1", function (d) {
					return (d.source === self.model.get("rootNode") || d.target === self.model.get("rootNode"));
				})
				.classed("node-2", function (d) {
					return (self.model.getDepthNeighbor(d.source) === 2 && self.model.getDepthNeighbor(d.target) === 1) || (self.model.getDepthNeighbor(d.target) === 2 && self.model.getDepthNeighbor(d.source) === 1);
				})
				.classed("node-more", function (d) {
					var isNode1 = (d.source === self.model.get("rootNode") || d.target === self.model.get("rootNode"));
					var isNode2 = (self.model.getDepthNeighbor(d.source) === 2 && self.model.getDepthNeighbor(d.target) === 1) || (self.model.getDepthNeighbor(d.target) === 2 && self.model.getDepthNeighbor(d.source) === 1);
					return self.model.get("rootNode") !== "" & !isNode1 && !isNode2;
				})
				.attr("marker-end", function (d) {
					var isWritingReference = function (link) {
						if (link.referenceData) {
							for (var index = 0; index < link.referenceData.length; index++) {
								if (link.referenceData[index].referenceType === "WRITING") {
									return true;
								}
							}
						} else if (link.type === "denotes") {
							// If denotes, red if all references to the selector are writing type
							return self.model.isWritingDenote(link);
						}
						return false;
					}(d);
					// Target focus and mutliple target -> RED / ARROW
					if (d.target === self.model.get("rootNode") && isWritingReference)
						return "url(#targetingRefFocus)";
					// Target focus -> ARROW
					else if (d.target === self.model.get("rootNode"))
						return "url(#targetingFocus)";
					// Multiple target -> RED
					else if (isWritingReference)
						return "url(#targeting)";
					else
						return "url(#" + d.type + ")";
				})
				.classed("important-path", function (d) {
					if (d.referenceData) {
						// If reference, red if writing type
						for (var index = 0; index < d.referenceData.length; index++) {
							if (d.referenceData[index].referenceType === "WRITING") {
								return true;
							}
						}
					} else if (d.type === "denotes") {
						// If denotes, red if all references to the selector are writing type
						return self.model.isWritingDenote(d);
					}
					return false;
				});

			pathLink.select(".linkText")
				.attr("d", function (d) {
					// Take care of the orientation of the link to have a label well placed
					return arcPath(d.source.x > d.target.x, d);
				})


			pathLink.select("circle")
				.attr("cx", function (l) {
					return (l.source.x + l.target.x) / 2;
				})
				.attr("cy", function (l) {
					return (l.source.y + l.target.y) / 2;
				})
				.classed("node-1", function (d) {
					return (d.source === self.model.get("rootNode") || d.target === self.model.get("rootNode"));
				})
				.classed("node-2", function (d) {
					return (self.model.getDepthNeighbor(d.source) === 2 && self.model.getDepthNeighbor(d.target) === 1) || (self.model.getDepthNeighbor(d.target) === 2 && self.model.getDepthNeighbor(d.source) === 1);
				})
				.classed("node-more", function (d) {
					var isNode1 = (d.source === self.model.get("rootNode") || d.target === self.model.get("rootNode"));
					var isNode2 = (self.model.getDepthNeighbor(d.source) === 2 && self.model.getDepthNeighbor(d.target) === 1) || (self.model.getDepthNeighbor(d.target) === 2 && self.model.getDepthNeighbor(d.source) === 1);
					return self.model.get("rootNode") !== "" & !isNode1 && !isNode2;
				});


			filterNodes.select("input")
				.property("checked", function (d) {
					return _.contains(self.model.get("currentEntitiesTypes"), d);
				});

			filterLinks.select("input")
				.property("checked", function (d) {
					return _.contains(self.model.get("currentRelationsTypes"), d);
				});

		},

		/**
		 * Change root node and move it to the center
		 */
		selectAndMoveRootNode: function (d) {
			// Unselect & unfix the current nodeRoot
			this.model.get("rootNode").fixed = false;
			this.model.get("rootNode").selected = false;

			d.selected = true;
			d.fixed = true;

			// If we create the graph for the first time with an id to focus, it has no x y.
			if (d.x === undefined && d.y === undefined) {
				d.x = this.model.get("width") / 2;
				d.y = this.model.get("height") / 2;
			}

			/*
			 * Moving the new nodeRoot at the center in delta times
			 */
			var delta = 10;
			var deltaX = ((this.model.get("width") / 2) - d.x) / delta;
			var deltaY = ((this.model.get("height") / 2) - d.y) / delta;

			this.model.set({
				rootNode: d
			});

			// Move the nodeRoot
			// Call force.tick() manually, in a setTimeout to avoid instant move
			for (var i = 0; i < delta; i++) {
				setTimeout(function () {
					d.x += deltaX;
					d.y += deltaY;
					d.px = d.x;
					d.py = d.y;
					force.tick();
				}, 15 * i);
			}
		},

		dblclick: function (d) {
			// Stop the force to control manually the actions
			force.stop();
			// If the node selected is the root node, defocus it
			if (d === this.model.get("rootNode")) {
				this.model.set({
					rootNode: ""
				});
				// Don't use the selectAndMove function so unfix manually
				d.fixed = false;
				d.selected = false;
			} else {
				// Focus and move new root node
				this.selectAndMoveRootNode(d);
			}
			// The nodeRoot has been moved, we can restart the force
			force.start();

			nodeEntity.classed("nodeOver", false);
			nodeEntity.classed("neighborNodeOver", false);
		},

		/*
		 * Method to create the filters and add them to the html
		 */
		createFilters: function (model) {
			var self = this;
			filterNodes = d3.select("#filterContainerNodes").selectAll("div")
				.data(model.get("entitiesTypes"))
				.enter()
				.append("div")
				.attr("class", "checkbox-container")
				.append("label")
				.each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesTypes"), d);
						})
						.on("click", function (d, i) {
							// register on click event
							self.applyFilter("entities", d, this.checked);
							self.updateCheckAllEntities();

							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity." + d);
						});
				});

			filterLinks = d3.select("#filterContainerLinks").selectAll("div")
				.data(model.get("relationsTypes"))
				.enter()
				.append("div")
				.attr("class", "checkbox-container")
				.append("label").each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_link_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentRelationsTypes"), d);
						})
						.on("click", function (d, i) {
							// register on click event
							self.applyFilter("relations", d, this.checked);
							self.updateCheckAllRelations();

							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.relation." + d);
						});
					d3.select(this).append("svg")
						.attr("class", "filter-svg-arrow")
						.append("svg:defs").append("svg:marker")
						.attr("id", "markerArrow")
						.attr("viewBox", "0 -5 10 10")
						.attr("markerWidth", 4)
						.attr("markerHeight", 4)
						.attr("orient", "auto")
						.append("svg:path")
						.classed("reference-filter", function (f) {
							return f === "WRITING";
						})
						.attr("d", "M0,-5L10,0L0,5");
					d3.select(this).select("svg").append("svg:line")
						.attr("class", "filter-arrow")
						.classed("reference-filter", function (f) {
							return f === "WRITING";
						})
						.attr("x1", "0")
						.attr("y1", "5")
						.attr("x2", "10")
						.attr("y2", "5")
						.attr("marker-end", "url(#markerArrow)");
				});

		},

		applyFilter: function (arrayUpdated, type, checked) {
			force.stop();
			this.model.updateArrayTypes(arrayUpdated, type, checked);
			this.model.updateEntitiesShown();
			if (arrayUpdated === "entities") {
				if (this.model.get("rootNode") !== "" && !_.contains(this.model.get("currentEntities"), this.model.get("rootNode"))) {
					// Vu que si on a plus rien d'afficher, on ne fait pas le move, on a toujours l'ancienne valeur pour la root node. Risque de Bug.
					//					if(this.model.get("currentEntities").length > 0) {
					//						this.selectAndMoveRootNode(this.model.get("currentEntities")[0]);
					//					}
					// Mis en com' car mtn on peut ne pas avoir de focus sans que ce soit gênant

					// unfix the root and set null to the root
					this.model.get("rootNode").fixed = false;
					this.model.get("rootNode").selected = false;
					this.model.set({
						rootNode: ""
					});
				}
			} else {
				this.model.updateRelationsShown();
			}

			this.update(this.model);
			force.start();
		},

		/*
		 * Update the checkbox for the entities according to the model
		 */
		updateCheckAllEntities: function () {
			if (this.model.get("currentEntitiesTypes").length === this.model.get("entitiesTypes").length) {
				$("#checkbox-all-entities").prop('checked', true);
			} else {
				$("#checkbox-all-entities").prop('checked', false);
			}
		},

		/*
		 * Update the checkbox for the relations according to the model
		 */
		updateCheckAllRelations: function () {
			if (this.model.get("currentRelationsTypes").length === this.model.get("relationsTypes").length) {
				$("#checkbox-all-relations").prop('checked', true);
			} else {
				$("#checkbox-all-relations").prop('checked', false);
			}
		},
	});

	function rescale() {
		// savedScale no null move a node
		if (savedScale !== null) {
			// update the zoom object in order to have the last position of the scale
			pan.scale(savedScale);
		}
		// savedTranslate no null move a node
		if (savedTranslate !== null) {
			// update the zoom object in order to have the last position of the translation
			pan.translate(savedTranslate);
		}

		// Rescale if we are moving a node
		if (!onMouseDownNode) {
			trans = d3.event.translate;
			scale = d3.event.scale;

			savedScale = null;
			savedTranslate = null;

			svg.attr("transform",
				"translate(" + trans + ")" + " scale(" + scale + ")");
		} else {
			// In this case, we are moving node, so save the scale/pan to update the zoom object
			savedScale = pan.scale();
			savedTranslate = pan.translate();
		}
	};

	/*
	 * Call when click on a node. Move or "zoomIn" the node
	 * param d: Node clicked
	 */
	function mousedown(d) {
		// Flag to avoid the rescale
		onMouseDownNode = true;

		// "ZoomIn" details 
		if (d3.event.shiftKey) {
			switch (d.type) {
			case "place":
				appRouter.navigate("#places/" + d.id, {
					trigger: true
				});
				break;
			case "program":
				appRouter.navigate("#programs/" + d.id, {
					trigger: true
				});
				break;
			case "service":
				appRouter.navigate("#services/types/" + d.deviceType, {
					trigger: true
				});
				break;
			case "device":
				appRouter.navigate("#devices/" + d.id, {
					trigger: true
				});
				break;
			case "time":
			case "selector":
			default:
				break;
			}
		} 
		else {
			// Fix node management
			d.hasBeenMoved = false;
			d3.select(this).on("mousemove", function () {
				d.hasBeenMoved = true;
			});
		}
	};

	/*
	 * Call when mouseup on a node. After a move of a node, fix it
	 * param d: Node moved HTML
	 * param nodeElement : Node moved D3
	 * param nodeRoot : Node root D3
	 */
	function mouseup(d, nodeElement, nodeRoot) {
		// don't defix the nodeRoot
		if (d !== nodeRoot) {
			// if the node has been move fix it
			if (d.hasBeenMoved) {
				d.fixed = true;
			} else {
				d.fixed = !d.fixed;
			}
			// Unbind on the mousemove event
			d3.select(nodeElement).on("mousemove", null);
			if (force.alpha() === 0) {
				force.start();
			}
		}

		// reset the flag for the rescale
		onMouseDownNode = false;
	};

	function arcPath(turned, d) {
		var dx = d.target.x - d.source.x,
			dy = d.target.y - d.source.y,
			dr = 150 / d.linknum; //linknum is defined above

		if (turned) {
			// If source.x > source.y, have to return the link by sweeping target and source, but also sweep it or it angle will be opposed
			return "M" + d.target.x + "," + d.target.y + "L" + d.source.x + "," + d.source.y;
		} else {
			return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
		}
	};

	return GraphView;
});