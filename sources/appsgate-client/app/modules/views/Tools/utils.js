define( function() {
  Math.easeInOutQuad = function (t, b, c, d) {
    t /= d/2;
    if (t < 1) return c/2*t*t + b;
    t--;
    return -c/2 * (t*(t-2) - 1) + b;
  };
  window.requestAnimFrame = (function(){
    return  window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || function( callback ){window.setTimeout(callback, 1000 / 60);};
  })();

  var pDebug = null, nb = 0;
  var AppsGateUtils = {

    generateSubscribers : function(C, name) {
      C['initSubscribers'+name] = function() {
        this['L_CB_'+name] = {};
      };
      C['Subscribe'+name] = function(id, CB) {
        this['L_CB_'+name][id] = CB;
      };
      C['UnSubscribe'+name] = function(id) {
        delete this['L_CB_'+name][id];
      };
      C['CallSubscribers'+name] = function() {
        for(var i in this['L_CB_'+name]) {
          this['L_CB_'+name][i].apply(this, arguments);
        }
      };
    },

    // --- Animation ---
    L_CB		: [],

    animate	: function(ms, CB) {
      pDebug = true;
      if(!pDebug) {
        pDebug = document.createElement('p');
        pDebug.setAttribute('id', 'pDebug');
        pDebug.style.position = 'fixed';
        pDebug.style.top  = '0px';
        pDebug.style.left = '0px';
        pDebug.innerText = 'Debug';
        document.body.appendChild( pDebug );
      }
      var now = Date.now();
      var obj = {
        CB : CB,
        duration : ms,
        start : now,
        end : now + ms
      };
      this.L_CB.push( obj );
      CB( {
        ms:now,
        dt:0
      });

      this.animFrame();
    },

    animFrame	: function() {
      if(this.isAnimating) return;

      this.isAnimating = true;
      var self = this;
      pDebug.innerText = nb++;
      window.requestAnimFrame( function() {
        var now = Date.now();
        var L = self.L_CB, dt, o, ms;
        self.L_CB = [];
        for(var i=0; i<L.length; i++) {
          o = L[i];
          ms = Math.min(o.duration, now - o.start);
          dt = ms  / o.duration;
          o.CB( {ms:ms,dt:dt} );
          if(dt < 1) {self.L_CB.push(o);}
        }
        self.isAnimating = false;
        if(self.L_CB.length) self.animFrame();
      });
    }
  };

  return AppsGateUtils;
});
