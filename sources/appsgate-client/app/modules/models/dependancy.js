define([
    "app",
    "models/brick",
], function (App, Brick) {

	var Dependancy = {};

	/**
	 * Dependancies Model class extending the Backbone model class and an abstract class for all the bricks in the application (universes, places, devices, services, programs...)
	 */
	Dependancy = Brick.extend({
		initialize: function () {
			var self = this;

			// Initialize types
			self.set({
				rootNode: "",
				height: 800,
				width: 960,
				mapDepthNeighbors: {},
				entitiesTypes: ["place", "program", "service", "time", "device", "selector"],
				relationsTypes: ["reference", "isLocatedIn", "isPlanified", "denotes"],
				currentEntitiesTypes: ["place", "program", "service", "time", "device", "selector"],
				currentRelationsTypes: ["reference", "isLocatedIn", "isPlanified", "denotes"]
			});

			// listen to the event when the data are received
			dispatcher.on("dataGraphReady", function (event) {
				self.set({
					entities: event.entities,
					relations: event.relations,
					neighbors: event.neighbors,
					currentEntities: event.entities,
					currentRelations: event.relations
				});

			});

			self.on("change:rootNode", function (model) {
				// If the root node is not null, calculate the new mapDepthNeighbors
				if (model.get("rootNode") !== "") {
					var reverseMap = self.buildReverseDepthNeigborsMap(self.buildDepthNeighborsMap(model.get("rootNode"),
						model.get("entities")));
					model.set({
						mapDepthNeighbors: reverseMap
					});
				} else {
					// Else reinitialize the map, we have defocus the curent rootNode
					model.set({
						mapDepthNeighbors: {}
					});
				}
			});
		},

		loadData: function (jsonData) {
			// Creation of the data structure : relations / entities / neighbors
			var relationsNotSorted = jsonData.links,
				entities = identifyService(jsonData.nodes),
				relations = processRelationsID(relationsNotSorted, entities),
				neighbors = buildNeighborsMap(relations);

			console.log("entities : %o", entities);

			dispatcher.trigger("dataGraphReady", {
				entities: entities,
				relations: relations,
				neighbors: neighbors
			});
		},

		/**
		 * Method to make a map to have the node depth from a root node
		 * @param: rootNode - Root node from where the algorithm begins
		 * @param: entities - Array of entities
		 * @return: map depth:neighbors
		 */
		buildDepthNeighborsMap: function (rootNode, entities) {
			var self = this;
			resetCheckMark(entities);

			/*
			 * mapResult : Map which be returned. It is map of <Depth><Array of node>, which save the nodes for each depth from the nodeRoot
			 * queueNode : Queue of node, use to process over all the node one time
			 * depthChildrenQueue : Queue of {depth,children} which is used to know how many children have a depth.
			 */
			var mapResult = [],
				queueNode = [],
				depthChildrendQueue = [];

			// depthChildren for the nodeRoot, with a 0 depth and children to 0 to force it to be delete at the fist iteration
			var firstDepthChildren = {
				"depth": 0,
				"children": 0
			};

			depthChildrendQueue.push(firstDepthChildren);
			queueNode.push(rootNode);
			rootNode.checkedNeighbor = true;

			while (queueNode.length > 0) {

				var currentNode = queueNode.shift();
				var currentDepthChild = depthChildrendQueue[0];

				// decrement the number of children with this depth, to count the current node
				currentDepthChild.children--;
				// if there is no other child for this depth, delete it from the queue
				if (currentDepthChild.children <= 0) {
					depthChildrendQueue.shift();
				}

				// Add the node to the map, create the index if no already done
				if (mapResult[currentDepthChild.depth]) {
					mapResult[currentDepthChild.depth].push(currentNode);
				} else {
					mapResult[currentDepthChild.depth] = [];
					mapResult[currentDepthChild.depth].push(currentNode);
				}

				var nbNeighbors = 0;
				// Add the children of this node, to nodes queue, and mark them
				entities.forEach(function (node) {
					if (self.neighboring(currentNode, node) && !node.checkedNeighbor) {
						// node is a child because neighbor and not checked
						queueNode.push(node);
						node.checkedNeighbor = true;
						nbNeighbors++;
					}
				});

				// Create a depthChildren object for its children. When it is their turn to be processed, this object sould be also on the first of the queue of the depthChildrenQueue
				var newDepthChildren = {
					"depth": currentDepthChild.depth + 1,
					"children": nbNeighbors
				};
				// Test children for the leaf cases
				if (newDepthChildren.children > 0) {
					depthChildrendQueue.push(newDepthChildren);
				}
			}

			return mapResult;
		},

		/**
		 * Create a reverse map : <node:depth> to access directly to the depth of a node
		 */
		buildReverseDepthNeigborsMap: function (mapDepthNeighbors) {
			var newReverseMapDepthNeighbors = new Object();
			for (var i = 0; i < mapDepthNeighbors.length; i++) {
				for (var j = 0; j < mapDepthNeighbors[i].length; j++) {
					newReverseMapDepthNeighbors[mapDepthNeighbors[i][j].id] = new Object();
					newReverseMapDepthNeighbors[mapDepthNeighbors[i][j].id] = i;
				}
			}
			return newReverseMapDepthNeighbors;
		},

		/**
		 * Return true if the entities a & b are neighbors
		 */
		neighboring: function (a, b) {
			var neighbors = this.get("neighbors");
			return neighbors[a.id + "," + b.id] || neighbors[b.id + "," + a.id];
		},

		/**
		 * Return the depth from the node root of a node.
		 * @param: Node target
		 */
		getDepthNeighbor: function (node) {
			return (this.get("mapDepthNeighbors")[node.id] !== undefined) ? this.get("mapDepthNeighbors")[node.id] : -1;
		},

		/**
		 * Return a distance according to the depth of the param node
		 * @param: Node whose we return the distance
		 */
		getLinkDistance: function (node) {
			switch (this.getDepthNeighbor(node)) {
			case 0:
				return 150;
			case 1:
				return 100;
			case 2:
				return 50;
			case 3:
				return 40;
			default:
				return 40;
			}
		},

		updateEntitiesShown: function () {
			var self = this;

			var newEntities = self.get("entities").filter(function (e) {
				return _.contains(self.get("currentEntitiesTypes"), e.type);
			});
			self.set({
				currentEntities: newEntities
			});

			var newLinks = buildLinksFromNodesShown.bind(this)();
			self.set({
				currentRelations: newLinks
			});

		},

		updateRelationsShown: function ()  {
			var newLinks = buildLinksFromNodesShown.bind(this)();
			this.set({
				currentRelations: newLinks,
				//                neighbors: buildNeighborsMap(newLinks)
			});
		},

		updateArrayTypes: function (array, type, checked) {
			var arrayUpdated = (array === "entities") ? this.get("currentEntitiesTypes") : this.get("currentRelationsTypes");
			if (checked) {
				arrayUpdated.push(type);
			} else {
				if (arrayUpdated.indexOf(type) !== -1)
					arrayUpdated.splice(arrayUpdated.indexOf(type), 1);
			}
		}

	});

	/**
	 * Method to build the map of the neighbor, ie : [id1:id2] -> 1, means entity id1 & id2 neighbors
	 * @param: relations - The array of relation on which we base to make the map
	 */
	function buildNeighborsMap(relations) {
		var neighbors = {};
		relations.forEach(function (d) {
			neighbors[d.source.id + "," + d.target.id] = 1;
		});

		return neighbors;
	}

	function identifyService(entities) {
		entities.forEach(function (e) {

			if (e.deviceType !== undefined) {
				var type = parseInt(e.deviceType);
				switch (type) {
				case 36:
					// Leave the media server because it does not appears in IHM
					entities.splice(entities.indexOf(e), 1);
					break;
				case 102:
					// Service mail a no attribute for the name, so take this in the client
					e.type = "service";
					e.name = $.i18n.t("services.mail.name.singular");
					break;
				case 103:
					// Weather case, show the location
					e.type = "service";
					e.name = e.location;
					break
				default:
					break;
				}
			}

			// Special case of the unlocated place. Set his name.
			if (e.id === "-1" && e.type === "place") {
				e.name = $.i18n.t("places-details.place-no-name");
			}
		});
		return entities;
	};

	/**
	 * Method to process relations and make an array of relations with a reference of entities. Done the first time of loading data. After this, the process of relations will be differents.
	 * @param: relationsNoSorted - array of relations not processed
	 * @param: entities - array of entities needed to process the relations
	 * @return: Array of relations processed with Object replacing the id
	 */
	function processRelationsID(relationsNoSorted, entities) {
		var relations = [];
		// Création du tableau des liens à partir de celui des nodes, en utilisant les objets comme source/target des liens et non les index comme c'est le cas par defaut pour la librairie
		relationsNoSorted.forEach(function (e) {
			var sourceNode = entities.filter(function (n) {
					return n.id === e.source;
				})[0],
				targetNode = entities.filter(function (n) {
					return n.id === e.target;
				})[0];

			// !!!!!!!!! On ne met pas une relation si une des entités de la relation est indéfinie, donc potentiellement on enlève de l'info
			if (typeof sourceNode !== 'undefined' && typeof targetNode !== 'undefined') {
				relations.push({
					source: sourceNode,
					target: targetNode,
					type: e.type
				});
			}

		});

		// Traitement pour pouvoir avoir plusieurs liens orienté pareil entre deux entités
		//sort links by source, then target
		relations.sort(function (a, b) {
			if (a.source.id > b.source.id) {
				return 1;
			} else if (a.source.id < b.source.id) {
				return -1;
			} else {
				if (a.target.id > b.target.id) {
					return 1;
				}
				if (a.target.id < b.target.id) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		//any links with duplicate source and target get an incremented 'linknum'
		for (var i = 0; i < relations.length; i++) {
			if (i != 0 &&
				relations[i].source == relations[i - 1].source &&
				relations[i].target == relations[i - 1].target) {
				relations[i].linknum = relations[i - 1].linknum + 1;
			} else {
				relations[i].linknum = 1;
			};
		};

		return relations;
	};

	/*
	 * Reset the check mark for all the node array (marks used to set the deepth of the nodes)
	 */
	function resetCheckMark(entities) {
		entities.forEach(function (node) {
			node.checkedNeighbor = false;
		});
	};

	function buildLinksFromNodesShown() {
		var self = this;
		var newLinks = [];

		self.get("relations").forEach(function (e) {
			var sourceNode = self.get("currentEntities").filter(function (n) {
					return n === e.source;
				})[0],
				targetNode = self.get("currentEntities").filter(function (n) {
					return n === e.target;
				})[0];

			if (typeof sourceNode !== 'undefined' && typeof targetNode !== 'undefined' && _.contains(self.get("currentRelationsTypes"), e.type)) {
				newLinks.push({
					source: sourceNode,
					target: targetNode,
					type: e.type
				});
			}
		});

		// Traitement pour pouvoir avoir plusieurs liens orienté pareil entre deux entités
		//sort links by source, then target
		newLinks.sort(function (a, b) {
			if (a.source.id > b.source.id) {
				return 1;
			} else if (a.source.id < b.source.id) {
				return -1;
			} else {
				if (a.target.id > b.target.id) {
					return 1;
				}
				if (a.target.id < b.target.id) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		//any links with duplicate source and target get an incremented 'linknum'
		for (var i = 0; i < newLinks.length; i++) {
			if (i != 0 &&
				newLinks[i].source == newLinks[i - 1].source &&
				newLinks[i].target == newLinks[i - 1].target) {
				newLinks[i].linknum = newLinks[i - 1].linknum + 1;
			} else {
				newLinks[i].linknum = 1;
			};
		};

		// Mise à jour de la map qui nous donne les voisins
		//        self.set({
		//            neighbors: buildNeighborsMap(newLinks)
		//        });

		return newLinks;
	};

	// Return the reference to the Dependancy constructor
	return Dependancy;
});