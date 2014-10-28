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
                relationsTypes: ["reference", "isLocatedIn", "isPlanified"]
                //                relationsTypes: ["listenTo", "actsOn", "isPlanified", "isLocatedIn", "denotes"]
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

                console.log(event.relations);
            });

            self.on("change:rootNode", function (model) {
                model.set({
                    mapDepthNeighbors: buildDepthNeighborsMap(model.get("rootNode"),
                    model.get("currentEntities"))
                });
            });
        },

        loadData: function (jsonData) {
            // Creation of the data structure : relations / entities / neighbors
            var relationsNotSorted = jsonData.links,
                entities = jsonData.nodes,
                relations = processRelationsID(relationsNotSorted, entities),
                neighbors = buildNeighborsMap(relations);

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
                    if (this.neighboring(currentNode, node) && !node.checkedNeighbor) {
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
         * Return true if the entities a & b are neighbors
         */
        neighboring: function (a, b) {
            var neighbors = this.get("neighbors");
            return neighbors[a.id + "," + b.id] || neighbors[b.id + "," + a.id];
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

        // Mise à jour de la map qui nous donne les voisins : linkedByIndex
        //        buildNeighborsMap(relations);
        // Mise à jour de la map qui nous donne les voisins du noeud racine par rapport à leur profondeur
        //        mapNeighborDeepth = makeDeepthNeighborsMapBreadthFirst(nodeRoot);

        //        console.log("nodesArray : %o", nodesArray);
        //        console.log("linksArray : %o", linksArray);
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

    // Return the reference to the Dependancy constructor
    return Dependancy;
});