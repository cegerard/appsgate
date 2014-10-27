define([
    "text!templates/dependancy/graph.html"
], function (GraphTemplate) {

    var GraphView = {};
    // detailled view of a debugger
    GraphView = Backbone.View.extend({
        template: _.template(GraphTemplate),

        initialize: function () {
            var svg, force;
        },

        render: function () {
            // render the editor with the program
            this.$el.append(this.template({
                dependancy: this.model,
            }));

            var height = 800,
                width = 960;

            svg = d3.select("#graph").append("svg")
                .attr("width", width)
                .attr("height", height);

            svg.append("svg:g").attr("id", "groupNode");

            force = d3.layout.force()
                .size([width, height])
                .gravity(0.03)
                .on("tick", tick);

            update(this.model.get("entities"), this.model);

            force.start();

        },

    });


    function update(nodes, model) {
        force.nodes(nodes);

        node = svg.select("#groupNode").selectAll(".ANODE").data(nodes, function (d) {
            return d.id;
        });

        nEnter = node.enter().append("svg:g")
            .attr("class", "ANODE")
            .call(force.drag)
            .each(function (a) {
                d3.select(this)
                    .append("circle")
                    .attr("class", "circleNode")
                    .attr("cx", function (m) {
                        return 0
                    })
                    .attr("cy", function (m) {
                        return 0
                    })
                    .attr("r", 0);
                //                .style("fill", function (n) {
                //                    if (n.type === "device") {
                //                        return "red";
                //                    }
                //                    if (n.type === "place") {
                //                        return "yellow";
                //                    }
                //                    if (n.type === "program") {
                //                        return "blue";
                //                    }
                //                    return "black";
                //                })
                //                .transition().duration(700).attr("r", 14);
                //                .attr("r", 14);
            });

        nEnter.select("circle")
            .transition().duration(800).style("fill", "red")
            .transition().duration(800).attr("r", 14);

        node.exit().select("circle").transition().duration(800).attr("r", 0);
        node.exit().transition().duration(800).remove();
    }


    function tick(e) {

        node
            .attr("transform", function (d) {
                return "translate(" + (d.x) + "," + (d.y) + ")";
            })
    }


    return GraphView;
});