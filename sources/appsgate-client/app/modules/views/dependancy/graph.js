define([
    "text!templates/dependancy/graph.html"
], function (GraphTemplate) {

    var GraphView = {};
    // detailled view of a debugger
    GraphView = Backbone.View.extend({
        template: _.template(GraphTemplate),

        initialize: function () {},

        render: function () {
            // render the editor with the program
            this.$el.append(this.template({
                dependancy: this.model,
            }));

            var width = this.model.get("width"),
                height = this.model.get("height");

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
                .data(["targeting", "reference", "isLocatedIn", "isPlanified"])
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

        },

        tick: function (e) {
            var self = this;

            node
                .attr("transform", function (d) {
                    return "translate(" + (d.x) + "," + (d.y) + ")";
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

            node.selectAll("text")
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
                });
        },

        update: function (model) {
            force.nodes(model.get("currentEntities"));
            force.links(model.get("currentRelations"));


            /******* NODES (=Entities) *******/

            // Bind node to the currentEntities, a node is represented by her ID 
            node = svg.select("#groupNode").selectAll(".nodeGroup").data(force.nodes(), function (d) {
                return d.id;
            });

            // New nodes
            var nEnter = node.enter().append("svg:g")
                .attr("class", "nodeGroup")
                .call(force.drag)
                .on("dblclick", this.click.bind(this))
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

            node.exit().select("circle").transition().duration(800).attr("r", 0);
            node.exit().transition().duration(800).remove();


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


                    d3.select(this).append("text")
                        .attr("class", "linklabel")
                        .style("font-size ", "13px ")
                        .attr("x", "50")
                        .attr("y", "20 ")
                        .attr("text-anchor", "middle ")
                })


            pathLink.exit().remove();

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
                    //            return 1;
                })
                .charge(function (d) {
                    if (d === model.get("rootNode")) {
                        return -450;
                    } else {
                        return -100;
                    }
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

            //            node.classed("nodeOver", false);
            //node.classed("neighborNodeOver", false);
            //    texts.classed("nodeOver", false);
            //    texts.classed("neighborNodeOver", false);
            //    texts.classed("nodeRoot", function (d) {
            //        return d === nodeRoot;
            //    });
            //    circleNode.classed("nodeOver", false);
            //    circleNode.classed("neighborNodeOver", false);
            //            node.select("circle").classed("nodeOver", false);
            //            node.select("circle").classed("neighborNodeOver", false);
            //
            //            node.select("text").classed("nodeOver", false);
            //            node.select("text").classed("neighborNodeOver", false);
            //            node.select("text").classed("nodeRoot", function (d) {
            //                return d === nodeRoot;
            //            });
        }


    });

    return GraphView;
});