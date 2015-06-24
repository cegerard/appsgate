define([
  "underscore"
  ], function(_) {

    /**
    * @constructor
    */
    function Communicator(serverAddr) {
      var self = this;

      // cannot be called as a function...
      if (!(this instanceof Communicator)) {
        throw new TypeError("Communicator constructor cannot be called as a function");
      }

      // save the address
      this.serverAddr = serverAddr;
    }

    /**
    * Communication layer between the front-end and the backend
    * @class Communicator
    */
    Communicator.prototype = {
      constructor: Communicator,

      /**
      * Set the callback for the websocket connection
      */
      initialize:function() {
        var self = this;
        
        // if websockets are not supported, return
        if (typeof WebSocket === "undefined") {
          alert("WebSocket is not supported...");
          return;
        }

        // open a websocket
        this.webSocket = new WebSocket(this.serverAddr);
        this.webSocket.onopen = function () {
          connection.send('Ping'); // Send the message 'Ping' to the server
        };

        // Log errors
        this.webSocket.onerror = function (error) {
          console.log('WebSocket Error ' + error);
        };

        // Log messages from the server
        this.webSocket.onmessage = function (e) {
          console.log('Server: ' + e.data);
        };


        // socket closed event or error occured during the connection - show the error in the modal settings
        this.webSocket.onclose = function() {
          console.log("Socket closed");
          dispatcher.trigger("WebSocketClose");
        };

        this.webSocket.onerror = function(error) {
          console.log(error);
          dispatcher.trigger("WebSocketClose");
        };

        // socket opened event - broadcast an event to the application
        this.webSocket.onopen = function() {
          console.log("Socket opened");
          dispatcher.trigger("WebSocketOpen");
        };

        // message received on the socket
        this.webSocket.onmessage = this.handleMessage;
      },

      /**
      * Getter for the address of the websocket address
      *
      * @return Websocket address
      */
      getServerAddr:function() {
        return this.serverAddr;
      },

      /**
      * Setter for the websocket server address
      *
      * @param serverAddr Websocket address of the server
      */
      setServerAddr:function(serverAddr) {
        this.serverAddr = serverAddr;
      },
      
      getServerName: function() {
        if (this.serverAddr !== undefined) {
          return this.serverAddr.split("://")[1].split(":")[0];
        }
        return "";
      },
      
      getServerPort: function() {
        if (this.serverAddr !== undefined) {
          return this.serverAddr.split(":")[2];
        }
        return "";
      },

      /**
      * Close the current connection if needed and start a new connection
      */
      reconnect:function() {
        // if there is already a connection, close it
        if (typeof this.webSocket !== "undefined") {
          // disactivate the close event management
          this.webSocket.onclose = null;

          // close the connection
          this.webSocket.close();

          delete this.webSocket;
        }

        // initialize the new connection
        this.initialize();
      },

      /**
      * @method handleMessage
      * Receives a message from the backend and dispatches it to the recepient module
      */
      handleMessage:function(message) {
        // rebuild the message for the application
        var jsonMessage = JSON.parse(message.data);
 //       console.log("received", message);

        if (jsonMessage.callId !== undefined && jsonMessage.callId!="system") {
          if (typeof jsonMessage.value === "string") {
            jsonMessage.value = JSON.parse(jsonMessage.value);
          }
          dispatcher.trigger(jsonMessage.callId, jsonMessage.value);
        } else if (typeof jsonMessage.objectId !== "undefined") {
          var id = jsonMessage.objectId;
          dispatcher.trigger(id, jsonMessage);
        } else if (jsonMessage.callId === "system") {
          var id = "system";
          dispatcher.trigger(id, jsonMessage);
        } else {
          var commandName = _.keys(jsonMessage)[0];
          dispatcher.trigger(commandName, jsonMessage[commandName]);
        }
      },

      /**
      * @method sendMessage
      * Send a message to the backend. Global format of the protocol is:
      * { "commandName": { "key": value } }
      *
      * @param commandName Command name to send with the message
      * @param{string} messageData Data to send, typically an object
      * @param targetType Parameter used by the server to route the message. 0: AbstractObject, 1: ApAM component
      */
      sendMessage:function(message) {
        //console.log("sending", JSON.stringify(message));
        this.webSocket.send(JSON.stringify(message));
      },

      /**
      * Close the websocket connection
      */
      close:function() {
        this.webSocket.close();
      }
    };

    return Communicator;

  });
