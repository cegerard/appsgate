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
                .linkDistance(200)
                .gravity(0.03)
                .on("tick", tick);

            // Call update to update the state of the force (node/link)
            update(this.model);

            force.start();

        },

    });


    function update(model) {
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
                    .attr("class", "label")
                    .text(function (d) {
                        return d.name;
                    })
//                    .classed("nodeRoot", function (d) {
//                        return d === nodeRoot;
//                    })
//                    .classed("node-0", function (d) {
//                        return d === nodeRoot;
//                    })
//                    .classed("node-1", function (d) {
//                        return neighboring(d, nodeRoot);
//                    })
//                    .classed("node-2", function (d) {
//                        return getDeepthNeighbor(d, mapNeighborDeepth) === 2;
//                    })
//                    .classed("node-more", function (d) {
//                        return getDeepthNeighbor(d, mapNeighborDeepth) > 2 || getDeepthNeighbor(d, mapNeighborDeepth) === -1;
//                    });
            });
        //            .each(function (a) {
        //                d3.select(this)
        //                    .append("circle")
        //                    .attr("class", "circleNode")
        //                    .attr("cx", function (m) {
        //                        return 0
        //                    })
        //                    .attr("cy", function (m) {
        //                        return 0
        //                    })
        //                    .attr("r", 0);
        //            });

        nEnter.select("circle")
            .transition().duration(800).style("fill", function (n) {
                if (n.type === "device") {
                    return "red";
                }
                if (n.type === "place") {
                    return "yellow";
                }
                if (n.type === "program") {
                    return "blue";
                }
                return "black";
            })
            .transition().duration(800).attr("r", 14);

        node.exit().select("circle").transition().duration(800).attr("r", 0);
        node.exit().transition().duration(800).remove();


        /******* LINKS (=Relations) *******/

        pathLink = svg.select("#groupLink").selectAll(".linkG")
            .data(force.links(), function (d) {
                return d.source.id + "-" + d.target.id;
            });

        pathLink
            .enter().append("svg:g")
            .attr("class", "linkG")
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
    }


    function tick(e) {

        node
            .attr("transform", function (d) {
                return "translate(" + (d.x) + "," + (d.y) + ")";
            })

        pathLink.select("path")
            .attr("d", function (d) {
                var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = 150 / d.linknum; //linknum is defined above
                return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
            })
    }


    return GraphView;
});