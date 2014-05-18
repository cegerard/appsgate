define( [
  "views/tools/utils"
], function(utils) {

  /**
   * Dragging manager class
   */
  var DragManager = {
    can:null,
    id:0,
    rect:null,
    TabDraggable:Array(),
    TabDraggedNode:Array(),
    TabDraggingPtr:Array(),

    /**
     * constructor
     */
    init: function (svgCanvasId) {
      document.addEventListener("mouseup"    , this.stopDragMouse, false);
      document.addEventListener("touchend"   , this.stopDragTouch, false);
      document.addEventListener("touchcancel", this.stopDragTouch, false);
      document.addEventListener("mousemove"  , this.updateInteractionMouse, false);
      document.addEventListener("touchmove"  , this.updateInteractionTouch, false);
      if(svgCanvasId.constructor === SVGSVGElement) {
        this.can = svgCanvasId;
      }
      else {
        if(svgCanvasId.constructor === String) {
          this.can = document.getElementById(svgCanvasId);
          if(this.can !== SVGSVGElement) {
            throw('Invalid canvas id', svgCanvasId, ": It should have identified a SVG tag");
          }
        }
        else {
          throw('invalid reference to SVG canvas, must be a DOM reference or an id.');
        }
      }

      this.rect = this.can.createSVGRect();
      this.rect.width = 1;
      this.rect.height = 1;

      this.idMatrix = this.can.createSVGMatrix();

      this.mapTransform = {};
    },

    /**
     * Returns an unique id
     */
    getUid:function() {
      return 'ID_' + (this.id++);
    },

    /**
     * Returns the index of a given node by its id
     * @param idNode Id to evaluate
     */
    indexOfIdNode:function (idNode) {
      for (var i=0; i<this.TabDraggable.length; i++) {
        if (this.TabDraggable[i].idNodeStr == idNode) {
          return i;
        }
      }
      return -1;
    },

    /**
     * Returns the index of a given node
     * @param node Node to evaluate
     */
    indexOfNode:function (node) {
      for (var i=0; i<this.TabDraggable.length; i++) {
        if (this.TabDraggable[i].node == node) {
          return i;
        }
      }
      return -1;
    },

    /**
     * Returns the index of a given pointer by its id
     * @param idPtr Pointer ID
     */
    indexOfPointer:function (idPtr) {
      for (var i=0; i<this.TabDraggable.length; i++) {
        if ( this.TabDraggable[i].idPtr1 == idPtr||this.TabDraggable[i].idPtr2 == idPtr) {
          return i;
        }
      }
      return -1;
    },

    /**
     * Returns the ancestors of a given node
     * @param node Node to evaluate
     */
    getAncestors:function (node) {
      var tabRep = [];
      while(node.parentNode) {
        tabRep.push( node.parentNode );
        node = node.parentNode;
      }
      return tabRep;
    },

    /**
     * Returns the descendents of a  given node
     * @param node Node to evaluate
     */
    getDescendents:function (node) {
      var T_rep = [];
      var T_tmp = new Array(node);
      var n_tmp = null;
      while(T_tmp.length) {
        n_tmp = T_tmp.pop();
        for(var i in n_tmp.childNodes) {
          T_rep.push( n_tmp.childNodes[i] );
          T_tmp.push( n_tmp.childNodes[i] );
        }
      }
      return T_rep;
    },

    /**
     * Returns the intersection between two given tables
     * @param T1 Table to compare
     * @param T2 Table to compare
     */
    getIntersection:function (T1, T2) {
      var T = [];
      for(var i=0; i<T1.length; i++) {
        if (T2.indexOf(T1[i]) >= 0) {
          T.push(T1[i]);
        }
      }
      return T;
    },

    /**
     * Returns the difference between two given tables
     * @param T1 Table to compare
     * @param T2 Table to compare
     */
    getDifference:function (T1, T2) {
      var T = [];
      for(var i=0; i<T1.length; i++) {
        if (T2.indexOf(T1[i]) == -1) {
          T.push(T1[i]);
        }
      }
      return T;
    },

    /**
     * Add a node as draggable
     * @param node The node to add
     * @param conf The configuration to apply
     */
    addDraggable:function (node, conf) {
      eventNode = conf.eventNode || node;
      // Is it still draggable?
      if(this.indexOfNode(node) >= 0) {
        return;
      }
      // If not, create the object in charge of managing its drag
      var objDraggable = {};
      objDraggable.idNodeStr = node.id;
      objDraggable.node = node;
      objDraggable.uid = this.getUid();
      objDraggable.cbDrag = conf.cbDrag || function() {};
      objDraggable.cbZoom = conf.cbZoom || function() {};
      objDraggable.idPtr1 = null;
      objDraggable.pt1 = this.can.createSVGPoint();
      objDraggable.pt1p = this.can.createSVGPoint();
      objDraggable.idPtr2 = null;
      objDraggable.pt2 = this.can.createSVGPoint();
      objDraggable.pt2p = this.can.createSVGPoint();
      objDraggable.dependsOf = null;        // The ancestor node that is also draggable
      objDraggable.tabDepend = []; // The closest descendants nodes that are also draggable
      // Dependencies
      var tabAncestors   = this.getAncestors(node);
      var indexAncestor;
      var tabDescendents = this.getDescendents(node);
      var indexDescendent;
      // Add dependency with respect to ancestors
      for(var i=0; i<tabAncestors.length; i++) {
        // Is this ancestor still dragged?
        indexAncestor = this.indexOfNode(tabAncestors[i]);
        if (indexAncestor >= 0) {
          //console.log("\t\t" + T_ancestors[i].id + " is a draggable ancestor of " + node.id);
          // Add the node to the dependencies of the ancestor
          this.TabDraggable[indexAncestor].tabDepend.push( node );
          // Add to the dependancies of the node the ones of the ancestors that are part of node's descendants
          //   remove them from ancestors dependancies
          var TabIntersection = this.getIntersection(tabDescendents, this.TabDraggable[indexAncestor].tabDepend);
          objDraggable.tabDepend = TabIntersection;
          objDraggable.dependsOf = TabAncestors[i];
          for(var j=0; j<TabIntersection.length; j++) {
            var index = this.TabDraggable.indexOfIdNode( TabIntersection[j] );
            this.TabDraggable[index].dependsOf = node;
          }
          this.TabDraggable[indexAncestor].tabDepend = this.getDifference(this.TabDraggable[indexAncestor].tabDepend, TabIntersection);
          break;
        }
      }
      // Add dependency with respect to descendants
      var TabTmp = new Array(node);
      var nodeTmp, childTmp, idxChildTmp;
      while(TabTmp.length) {
        nodeTmp = TabTmp.pop();
        for(var k=0; k<nodeTmp.childNodes.length; k++) {
          childTmp   = nodeTmp.childNodes[k];
          idxChildTmp = this.indexOfNode(childTmp);
          // Is nc_tmp still dragged?
          if(idxChildTmp >= 0) {
            // Add nc_tmp to the dependencies
            objDraggable.tabDepend.push(childTmp);
            // Check that nc_tmp does not depend from any one
            if(this.TabDraggable[idxChildTmp].dependsOf) {

              console.log('node ' + childTmp.id + " should not depend on anyone... but " + node + ". It actually depends on " + this.TabDraggable[idxChildTmp].dependsOf);
            }
            // node is now its closest draggable ancestor
            this.TabDraggable[idxChildTmp].dependsOf = node;
          }
          else { // If not, then continue with its descendants
            TabTmp.push(childTmp);
          }
        }
      }
      this.TabDraggable.push( objDraggable );
      // Then subscribe to events
      eventNode.addEventListener("mousedown" , function(e) {DragManager.startDragMouse(e, node);}, false);
      eventNode.addEventListener("touchstart", function(e) {DragManager.startDragTouch(e, node);}, false);
    },

    /**
     * Removes a node from the draggable table
     * @param node Node to remove
     */
    removeDraggable:function (node) {
      var i = this.indexOfNode(idNodeStr);
      this.TabDraggable.splice(i, 1);
    },

    /**
     * Handles mouse startdrag event
     * @param e Mouse event
     * @param node Node to drag
     */
    startDragMouse:function (e, node) {
      var coords = DragManager.getCoordinateRelativeTo(e.pageX, e.pageY, DragManager.can);
      DragManager.startDrag(e, node, "mouse", coords.x, coords.y);
    },

    /**
     * Handles touch startdrag event
     * @param e Touch event
     * @param node Node to drag
     */
    startDragTouch:function (e, node) {
      e.preventDefault();
      var evt = null, coords = null;
      for(var i=0;i<e.changedTouches.length;i++) {
        evt    = e.changedTouches[i];
        coords = DragManager.getCoordinateRelativeTo(evt.pageX, evt.pageY, DragManager.can);
        DragManager.startDrag(evt, node, evt.identifier, coords.x, coords.y);
      }
    },

    /**
     * Handles dragging of a node
     * @param e Event triggering dragging
     * @param node Node to drag
     * @param idPtr Pointer id
     * @param x Coordinate X of the node relative to the canvas
     * @param y Coordinate Y of the node relative to the canvas
     */
    startDrag:function (e, node, idPtr, x, y) {
      // Still fully used?
      var i = this.indexOfNode(node);
      if( this.TabDraggedNode.indexOf(node) >= 0 && this.TabDraggable[i].idPtr2 !== null) {
        return;
      }
      // If not, start the drag
      // Compute coordinates of the point in the frame of the node
      // console.log("this.TabDraggable[i].idPtr1 = " + this.TabDraggable[i].idPtr1);
      var M = node.getCTM().inverse();
      if(this.TabDraggable[i].idPtr1 === null) {
        this.TabDraggable[i].idPtr1 = idPtr;
        this.TabDraggable[i].pt1.x = x;
        this.TabDraggable[i].pt1.y = y;
        this.TabDraggable[i].pt1 = this.TabDraggable[i].pt1.matrixTransform( M );
        this.TabDraggedNode.push(node);
      }
      else {
        this.TabDraggable[i].idPtr2 = idPtr;
        this.TabDraggable[i].pt2.x = x;
        this.TabDraggable[i].pt2.y = y;
        this.TabDraggable[i].pt2 = this.TabDraggable[i].pt2.matrixTransform( M );
        // console.log("----------------------------RotoZoom--------------------------------------------");
        // console.log("now using pointer2 = " + this.TabDraggable[i].idPtr2);
      }
      // Register
      this.TabDraggingPtr.push(idPtr);
      this.updateInteraction(idPtr, x, y);
      this.CallSubscribersAddPtr(idPtr, node, e.target);
    },

    /**
     * Handles the mouse stopdrag event
     * @param e Mouse event
     */
    stopDragMouse:function (e) {
      DragManager.stopDrag(e, "mouse");
    },

    /**
     * Handles the touch stopdrag event
     * @param e Touch event
     */
    stopDragTouch:function (e) {
      //e.preventDefault();
      for(var i=0;i<e.changedTouches.length;i++) {
        DragManager.stopDrag(e.changedTouches[i], e.changedTouches[i].identifier);
      }
    },

    /**
     * Handles the stopdrag event
     * @param e The stopdrag event
     * @param idPtr PointerId
     */
    stopDrag:function (e, idPtr) {
      var i = this.indexOfPointer(idPtr);
      if(i == -1) {
        return;
      }
      this.TabDraggingPtr.splice(this.TabDraggingPtr.indexOf(idPtr), 1);
      if(this.TabDraggable[i].idPtr1 == idPtr) {
        this.TabDraggable[i].idPtr1 = this.TabDraggable[i].idPtr2;
        this.TabDraggable[i].pt1.x = this.TabDraggable[i].pt2.x;
        this.TabDraggable[i].pt1.y = this.TabDraggable[i].pt2.y;
      }
      this.TabDraggable[i].idPtr2 = null;
      if(this.TabDraggable[i].idPtr1 === null) {
        this.TabDraggedNode.splice(this.TabDraggedNode.indexOf(this.TabDraggable[i].node), 1);
      }
      // this.TabDraggable[i].idPtr1 = null;
      this.CallSubscribersSubPtr(idPtr, e.target);
    },

    /**
     * Updates the view based on a mouse interaction
     * @param e Interaction mouse event
     */
    updateInteractionMouse:function (e) {
      var coords = DragManager.getCoordinateRelativeTo(e.pageX, e.pageY, DragManager.can);
      DragManager.updateInteraction("mouse", coords.x, coords.y);
    },

    /**
     * Updates the view based on a touch interaction
     * @param e Interaction touch event
     */
    updateInteractionTouch:function (e) {
      e.preventDefault();
      var evt = null, coords = null;
      for(var i=0;i<e.changedTouches.length;i++) {
        evt = e.changedTouches[i];
        coords = DragManager.getCoordinateRelativeTo(evt.pageX, evt.pageY, DragManager.can);
        DragManager.updateInteraction(evt.identifier, coords.x, coords.y);
      }
    },

    /**
     * Updates the view based on an interaction
     * @param idPtr Pointer Id
     * @param x Node coordinate X relative to the page
     * @param y Node coordinate Y relative to the page
     */
    updateInteraction:function (idPtr, x, y) {
      var i = this.TabDraggingPtr.indexOf(idPtr);
      if(i == -1) {
        return;
      }
      i = this.indexOfPointer(idPtr);
      var node = this.TabDraggable[i].node;
      // Apply translation
      // console.log("update while idPtr2 is " + this.TabDraggable[i].idPtr2);
      if(this.TabDraggable[i].idPtr2 !== null) {
        this.rotoZoom(i, idPtr, x, y);
      }
      else {
        this.drag(i, idPtr, x, y);
      }

      // Update depending nodes
      for(var j=0; j<this.TabDraggable[i].tabDepend.length; j++) {
        var indexChild = this.indexOfNode( this.TabDraggable[i].tabDepend[j] );
        var idPtr1 = this.TabDraggable[index_child].idPtr1;
        this.updateInteraction(idPtr1, px, py);
      }
    },

    /**
     * Performs zoom/rotation based on an interaction
     * @param i Pointer index
     * @param idPtr Pointer Id
     * @param x Node coordinate X relative to the page
     * @param y Node coordinate Y relative to the page
     */
    rotoZoom:function (i, idPtr, x, y) {
      var obj = this.TabDraggable[i];
      if(idPtr == this.TabDraggable[i].idPtr1) {
        obj.pt1p.x = x;
        obj.pt1p.y = y;
        obj.pt1p = obj.pt1p.matrixTransform( obj.node.parentNode.getCTM().inverse());
      }
      else {
        obj.pt2p.x = x; obj.pt2p.y = y;
        obj.pt2p = obj.pt2p.matrixTransform( obj.node.parentNode.getCTM().inverse());
      }
      // console.log("RotoZoom");
      /*console.log("RotoZoom <" + this.TabDraggable[i].pt1p.x + ';' + this.TabDraggable[i].pt1p.y + '>'
        +' <' + this.TabDraggable[i].pt2p.x + ';' + this.TabDraggable[i].pt2p.y + '>'
        + "------"
        + "<" + this.TabDraggable[i].pt1.x + ';' + this.TabDraggable[i].pt1.y + '>'
        +' <' + this.TabDraggable[i].pt2.x + ';' + this.TabDraggable[i].pt2.y + '>');
        */
      var dx  = this.TabDraggable[i].pt1.x - this.TabDraggable[i].pt2.x, dy  = this.TabDraggable[i].pt1.y - this.TabDraggable[i].pt2.y, dxp = this.TabDraggable[i].pt1p.x - this.TabDraggable[i].pt2p.x, dyp = this.TabDraggable[i].pt1p.y - this.TabDraggable[i].pt2p.y, s = 0, c = 0, e = 0, f = 0;

      if(dx === 0 && dy === 0) {
        console.log("points confondues!");
        return;
      }
      if(dx === 0) {
        c = dyp/dy;
      }
      else {
        if(dy === 0) {
          c = dxp/dx;
        }
        else{
          c = (dxp + s*dy)/dx;
        }
      }
      e = this.TabDraggable[i].pt1p.x - c*this.TabDraggable[i].pt1.x + s*this.TabDraggable[i].pt1.y;
      f = this.TabDraggable[i].pt1p.y - s*this.TabDraggable[i].pt1.x - c*this.TabDraggable[i].pt1.y;
      this.mapTransform[obj.uid] = {obj:obj,matrix:'matrix('+c+','+s+','+(-s)+','+c+','+e+','+f+')'};

      this.updateRender();
    },

    /**
     * Performs drag based on an interaction
     * @param i Pointer index
     * @param idPtr Pointer Id
     * @param x Node coordinate X relative to the page
     * @param y Node coordinate Y relative to the page
     */
    drag:function (i, idPtr, x, y) {
      // console.log("Drag with " + idPtr);
      var obj = this.TabDraggable[i];
      obj.pt1p.x = x;
      obj.pt1p.y = y;
      obj.pt1p = obj.pt1p.matrixTransform( obj.node.parentNode.getCTM().inverse());
      var mat = obj.node.parentNode.getCTM().inverse().multiply( obj.node.getCTM());
      var e = obj.pt1p.x - mat.a*obj.pt1.x - mat.c*obj.pt1.y, f = obj.pt1p.y - mat.b*obj.pt1.x - mat.d*obj.pt1.y;
      this.TabDraggable[i] = obj;
      this.mapTransform[obj.uid] = {obj:obj,matrix:'matrix('+mat.a+','+mat.b+','+mat.c+','+mat.d+','+e+','+f+')'};
      this.updateRender();
      // obj.node.setAttribute('transform', 'matrix('+mat.a+','+mat.b+','+mat.c+','+mat.d+','+e+','+f+')');
    },

    /**
     * Returns coordinates of an element relative to the given ones
     * @param x Coordinate X to compare
     * @param y Coordinate Y to compare
     * @param node Node to compare
     */
    getCoordinateRelativeTo:function (x, y, node) {
      var element = node, offsetX = 0, offsetY = 0, mx, my;
      var stylePaddingLeft = parseInt(document.defaultView.getComputedStyle(node, null).paddingLeft, 10)      || 0;
      var stylePaddingTop  = parseInt(document.defaultView.getComputedStyle(node, null).paddingTop, 10)       || 0;
      var styleBorderLeft  = parseInt(document.defaultView.getComputedStyle(node, null).borderLeftWidth, 10)  || 0;
      var styleBorderTop   = parseInt(document.defaultView.getComputedStyle(node, null).borderTopWidth, 10)   || 0;

      // Compute the total offset. It's possible to cache this if you want
      if (element.offsetParent !== undefined) {
        do {offsetX += element.offsetLeft;
          offsetY += element.offsetTop;
        } while ((element = element.offsetParent));
      }

      // Add padding and border style widths to offset
      // Also add the <html> offsets in case there's a position:fixed bar (like the stumbleupon bar)
      // This part is not strictly necessary, it depends on your styling
      offsetX += stylePaddingLeft + styleBorderLeft + document.body.parentNode.offsetLeft;
      offsetY += stylePaddingTop  + styleBorderTop  + document.body.parentNode.offsetTop;

      mx = x - offsetX;
      my = y - offsetY;

      // We return a simple javascript object with x and y defined
      return {x: mx, y: my};
    },

    /**
     * Applies queued transformations of the view
     */
    updateRender:function() {
      if(this.isUpdatingRender) {return;}
      this.isUpdatingRender = true;
      var self = this;
      window.requestAnimFrame( function() {
        self.isUpdatingRender = false;
        for(var i in self.mapTransform) {
          self.mapTransform[i].obj.node.setAttribute('transform', self.mapTransform[i].matrix);
          try {
            if(self.mapTransform[i].obj.idPtr2)
              self.mapTransform[i].obj.cbZoom(self.mapTransform[i].obj.node);
          } catch(e) {
            //alert('error' + e);
            console.warn(e);
          }
          // alert('arf');
        }
        self.mapTransform = {};
      });
    }
  };

  // Generate_accessors
  utils.generateSubscribers(DragManager, 'AddPtr');
  utils.generateSubscribers(DragManager, 'SubPtr');

  // return the reference to the drag manager
  return DragManager;
});






