define([
    "app",
    "text!templates/dependancy/graph.html"
], function (App, GraphTemplate) {

	var GraphView = {};

	GraphView = Backbone.View.extend({
		template: _.template(GraphTemplate),

		events: {
			"click button.refresh-button": "onRefreshButton",
			"click button.unfix-button": "onUnfixButton",
			"click button.search-button": "onSearchButton",
			"keyup .search-input-text": "onSearchButton",
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

		},

		onRefreshButton: function () {
			// Notify collection we want the new data
			dependancies.refresh();
			// Reload this page to refresh data
			appRouter.dependancies();
		},

		onSearchButton: function (e) {

			if (e.type === "keyup") {
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
				} else {
					// If there is node containing the string searched, highlight them
					nodeEntity.classed("neighborNodeOver", function (d) {
						return nodesFound.indexOf(d) !== -1;
					});

					// There is only one result, typing enter select it
					if (e.keyCode === 13 && nodesFound.length === 1) {
						force.stop();
						this.selectAndMoveRootNode(nodesFound[0]);
						$(".search-input-text").select();
						force.start();
					}
				}
			}
		},

		onUnfixButton: function (e) {
			var self = this;
			force.nodes().forEach(function (d) {
				if (self.model.get("rootNode") !== d) {
					d.fixed = false
				}
			});
			if (force.alpha() === 0) {
				force.start();
			}
		},

		render: function () {
			this.$el.html(this.template({
				dependancy: this.model,
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

			// Add the svg to html
			svg = d3.select("#graph").select("svg")
				.attr("width", width)
				.attr("height", height);

			//			svg.call(d3.behavior.zoom().on("zoom", rescale));

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
						return 38
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

			// Call update to update the state of the force (node/link)
			this.update(this.model);

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

			// New nodes
			var nEnter = nodeEntity.enter().append("svg:g")
				.attr("class", "nodeGroup")
				.call(force.drag)
				//                .on("click", this.click.bind(this))
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
							return 0
						})
						.attr("cy", function (m) {
							return 0
						})
						.attr("r", 0);
					// IMAGE
					d3.select(this).append("image")
						.attr("xlink:href", function (d) {
							var imgNode = "";
							if (d.type === "device") {
								imgNode = "/app/img/home/device3.svg";
							} else if (d.type === "time") {
								imgNode = "/app/img/home/calendar.svg";
							} else if (d.type === "place") {
								imgNode = "/app/img/home/place1.svg";
							} else if (d.type === "program") {
								imgNode = "/app/img/home/program2.svg";
							} else if (d.type === "service") {
								imgNode = "/app/img/home/service1.svg";
							}
							return imgNode;
						})
						.attr('opacity', 0)
						.attr('x', -12)
						.attr('y', -12)
						.attr('width', 24)
						.attr('height', 24);
					// TEXT
					d3.select(this).append("text")
						.attr("class", "label-name")
						.text(function (d) {
							return d.name;
						})
					
					// Type program, add form to indicate running or not. 
					if (a.type === "program") {
						if (a.state === "DEPLOYED" || a.state === "INVALID" || a.state === "INCOMPLETE") {
							d3.select(this).append("rect")
								.attr("class", "form-program")
								.attr("x", function (m) {
									return 0
								})
								.attr("y", function (m) {
									return 0
								})
								.attr("width", 8)
								.attr("height", 8)
								.attr('opacity', 0);
						} else if (a.state === "PROCESSING" || a.state === "LIMPING") {
							d3.select(this).append("svg:path")
								.attr("class", "form-program")
								.attr("d", "M0,-5L10,0L0,5L0,-5")
								.attr('opacity', 0);
						}

					}
				});

			nEnter.select("circle").transition().duration(800).attr("r", 14);
			nEnter.select("image").transition().duration(1000).style("opacity", 1);
			nEnter.select(".form-program").transition().duration(800).style("opacity", 1);
			nEnter.select("text").transition().duration(800).style("opacity", 1);

			nodeEntity.exit().select("image").transition().duration(600).style("opacity", 0);
			nodeEntity.exit().select("text").transition().duration(700).style("opacity", 0);
			nodeEntity.exit().select("circle").transition().duration(700).attr("r", 0);
			nodeEntity.exit().select(".form-program").transition().duration(700).style("opacity", 0);
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
						.attr("class", "circle-information hidden")
						.attr("r", 5)
						.attr("fill", "red")
						.on("mouseover", function (d) {
							d3.select(this.parentNode).select("text").classed("hidden", false);
						})
						.on("mouseout", function (d) {
							d3.select(this.parentNode).select("text").classed("hidden", true);
						});
					// TEXT
					d3.select(this).append("text")
						.attr("class", "linklabel linklabelholder hidden")
						.style("font-size", "13px")
						.attr("x", "90")
						.attr("y", "-10")
						.attr("text-anchor", "middle")
						.append("textPath")
						.attr("xlink:href", function (d) {
							return "#linkID_" + i;
						})
						.text(function (d) {
							return $.i18n.t("dependancy.type.relation." + d.type);
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
						return -450;
					} else {
						return -100;
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
					return self.model.getDepthNeighbor(d) > 2 || self.model.getDepthNeighbor(d) === -1;
				})
				.classed("fixedNode", function (d) {
					return d !== self.model.get("rootNode") && d.fixed && !$(this).hasClass("nodeOver");
				});

			nodeEntity.selectAll("text")
				.attr("transform", function (d) {
					if (d === self.model.get("rootNode")) {
						return "translate(0,-18)";
					} else {
						return "translate(0,-15)";
					}
				});

			nodeEntity.selectAll("circle")
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

			nodeEntity.selectAll(".form-program")
				.attr("transform", function (d) {
					return "translate(3,10)";
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
					return !isNode1 && !isNode2;
				})
				.attr("marker-end", function (d) {
					var isMultipleTargeted = self.model.isTargetMultipleTargeted(d.target);
					// Target focus and mutliple target -> RED / ARROW
					if (d.target === self.model.get("rootNode") && d.type === "reference" && isMultipleTargeted)
						return "url(#targetingRefFocus)";
					// Target focus -> ARROW
					else if (d.target === self.model.get("rootNode"))
						return "url(#targetingFocus)";
					// Multiple target -> RED
					else if (isMultipleTargeted && d.type === "reference")
						return "url(#targeting)";
					else
						return "url(#" + d.type + ")";
				})
				.classed("important-path", function (d) {
					return (d.type === "reference" && self.model.isTargetMultipleTargeted(d.target));
				});

			pathLink.select(".linkText")
				.attr("d", function (d) {
					// Take care of the orientation of the link to have a label well placed
					return arcPath(d.source.x > d.target.x, d);
				})


			pathLink.select("circle")
				.classed("hidden", function (l) {
					return !(l.source === self.model.get("rootNode") || l.target === self.model.get("rootNode"));
				})
				.attr("cx", function (l) {
					return (l.source.x + l.target.x) / 2;
				})
				.attr("cy", function (l) {
					return (l.source.y + l.target.y) / 2;
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

		click: function (d) {
			// Stop the force to control manually the actions
			//            force.stop();
			if (d !== this.model.get("rootNode")) {
				d.fixed = !d.fixed;
			}

			//            console.log("binding move");
			//            this.on("mousemove", function () {
			//                console.log("MOVE");
			//                d.fixed = true;
			//                this.onmouseup(function () {
			//                    this.off("mousemove");
			//                });
			//            });
			if (force.alpha() === 0) {
				force.start();
			}
			// The nodeRoot has been moved, we can restart the force
			force.start();
		},

		dblclick: function (d) {
			// Go to details manage
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
			// Focus / Unfocus
			else {
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
			}
		},

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
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.type.entity." + d);
						});
				});

			filterLinks = d3.select("#filterContainerLinks").selectAll("div")
				.data(model.get("relationsTypes"))
				.enter()
				.append("div")
				.attr("class", "checkbox-container")
				.append("label")
				.each(function (d) {
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
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.type.relation." + d);
						});
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

		updateCheckAllEntities: function () {
			if (this.model.get("currentEntitiesTypes").length === this.model.get("entitiesTypes").length) {
				$("#checkbox-all-entities").prop('checked', true);
			} else {
				$("#checkbox-all-entities").prop('checked', false);
			}
		},

		updateCheckAllRelations: function () {
			if (this.model.get("currentRelationsTypes").length === this.model.get("relationsTypes").length) {
				$("#checkbox-all-relations").prop('checked', true);
			} else {
				$("#checkbox-all-relations").prop('checked', false);
			}
		}

	});

	function rescale() {
		trans = d3.event.translate;
		scale = d3.event.scale;

		svg.select("#groupNode").attr("transform",
			"translate(" + trans + ")" + " scale(" + scale + ")");
		svg.select("#groupLink").attr("transform",
			"translate(" + trans + ")" + " scale(" + scale + ")");
	};

	function mousedown(d) {
		// disable zoom
		//		svg.call(d3.behavior.zoom().on("zoom", null));
		d.hasBeenMoved = false;
		d3.select(this).on("mousemove", function () {
			d.hasBeenMoved = true;
		});
	};

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

		// enable zoom
		//		svg.call(d3.behavior.zoom().on("zoom", rescale));
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