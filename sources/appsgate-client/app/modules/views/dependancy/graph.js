define([
    "text!templates/dependancy/graph.html"
], function (GraphTemplate) {

    var GraphView = {};

    GraphView = Backbone.View.extend({
        template: _.template(GraphTemplate),

        events: {
            "click button.refresh-button": "onRefreshButton"
        },

        initialize: function () {},

        onRefreshButton: function () {
            // Reload this page to refresh data
            appRouter.dependancies();
        },

        render: function () {
            this.$el.append(this.template({
                dependancy: this.model,
            }));

            var width = this.model.get("width"),
                height = this.model.get("height");

            this.createFilters(this.model);

            // Add the svg to html
            svg = d3.select("#graph").append("svg")
                .attr("width", width)
                .attr("height", height);

            // Add the div for links & nodes
            svg.append("svg:g").attr("id", "groupLink");
            svg.append("svg:g").attr("id", "groupNode");

            // Build the directional arrows for the links/edges
            // Per-type markers, as they don't inherit styles.
            svg.append("svg:defs").selectAll("marker")
                .data(["targeting", "reference", "isLocatedIn", "isPlanified", "denotes"])
                .enter().append("svg:marker")
                .attr("id", String)
                .attr("viewBox", "0 -5 10 10")
                .attr("refX", function (t) {
                    if (t === "targeting") {
                        return 38
                    } else {
                        return 28;
                    }
                })
                .attr("refY", -2)
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
        },

        update: function (model) {
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
                .on("dblclick", this.click.bind(this))
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
                });

            nEnter.select("circle")
                .transition().duration(800).attr("r", 14);

            nodeEntity.exit().select("image").transition().duration(600).style("opacity", 0);
            nodeEntity.exit().select("text").transition().duration(700).style("opacity", 0);
            nodeEntity.exit().select("circle").transition().duration(700).attr("r", 0);
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
                    d3.select(this).append("svg:path")
                        .attr("class", function (d) {
                            return "link " + d.type;
                        })
                        .attr("marker-end", function (d) {
                            return "url(#" + d.type + ")";
                        })
                        .attr("id", function (d) {
                            return "linkID_" + i;
                        })
                        .attr("refX", -30);


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
                            return d.type;
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
                });

            nodeEntity.selectAll("text")
                .attr("transform", function (d) {
                    if (d === self.model.get("rootNode")) {
                        return "translate(0,-18)";
                    } else {
                        return "translate(0,-15)";
                    }
                });


            pathLink.select("path")
                .attr("d", function (d) {
                    var dx = d.target.x - d.source.x,
                        dy = d.target.y - d.source.y,
                        dr = 150 / d.linknum; //linknum is defined above
                    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
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
                    if (d.target === self.model.get("rootNode"))
                        return "url(#targeting)";
                    else
                        return "url(#" + d.type + ")";
                })
                .classed("targeting", function (d) {
                    return d.target === self.model.get("rootNode");
                });

            pathLink.select("circle")
                .classed("hidden", function (l) {
                    return !(l.source === self.model.get("rootNode") || l.target === self.model.get("rootNode"));
                })
                .attr("cx", function (l) {
                    return (l.source.x + l.target.x) / 2;
                    //                    return calculateXCircle(l);
                })
                .attr("cy", function (l) {
                    return (l.source.y + l.target.y) / 2;
                    //                    return calculateXCircle(l);
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
            force.stop();
            this.selectAndMoveRootNode(d);

            // The nodeRoot has been moved, we can restart the force
            force.start();

            nodeEntity.classed("nodeOver", false);
            nodeEntity.classed("neighborNodeOver", false);
        },

        createFilters: function (model) {
            var self = this;
            d3.select("#filterContainerNodes").selectAll("div")
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
                        })
                    d3.select(this).append("span")
                        .text(function (d) {
                            return d;
                        });
                });

            d3.select("#filterContainerLinks").selectAll("div")
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
                        })
                    d3.select(this).append("span")
                        .text(function (d) {
                            return d;
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
                    if (this.model.get("currentEntities").length > 0) {
                        this.selectAndMoveRootNode(this.model.get("currentEntities")[0]);
                    }
                }
            } else {
                this.model.updateRelationsShown();
            }

            this.update(this.model);
            force.start();
        }


    });

    //    function calculateXCircle(link) {
    //        var A = link.source;
    //        var B = link.target;
    //        var nAB = Math.sqrt(Math.pow((B.x - A.x), 2) + Math.pow((B.y - A.y), 2));
    //        var nAM = nAB / 2;
    //        var radius = 150 / link.linknum;
    //        var sagitta = radius + Math.sqrt(Math.pow(radius, 2) - Math.pow(nAM, 2));
    //        var nAS = Math.sqrt(Math.pow(nAM, 2) + Math.pow(sagitta, 2));
    //        var theta = (B.y - A.y) / (B.x - A.x)
    //
    //        return nAS * Math.cos(theta);
    //    };
    //
    //    function calculateYCircle(link) {
    //        var A = link.source;
    //        var B = link.target;
    //        var nAB = Math.sqrt(Math.pow((B.x - A.x), 2) + Math.pow((B.y - A.y), 2));
    //        var nAM = nAB / 2;
    //        var radius = 150 / link.linknum;
    //        var sagitta = radius + Math.sqrt(Math.pow(radius, 2) - Math.pow(nAB, 2));
    //        var nAS = Math.sqrt(Math.pow(nAM, 2) + Math.pow(sagitta, 2));
    //        var theta = (B.y - A.y) / (B.x - A.x)
    //
    //        return nAS * Math.sin(theta);
    //    };

    return GraphView;
});