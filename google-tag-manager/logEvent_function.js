    try {
      var db = {{Debug Mode}}
      window.logEvent = function(name, params) {
        if (!name) {
          return;
        }
    
        if (window.AnalyticsWebInterface) {
          // Call Android interface
          window.AnalyticsWebInterface.logEvent(name, JSON.stringify(params));
        } else if (window.webkit
            && window.webkit.messageHandlers
            && window.webkit.messageHandlers.firebase) {
          // Call iOS interface
          var message = {
            command: 'logEvent',
            name: name,
            parameters: params
          };
          window.webkit.messageHandlers.firebase.postMessage(message);
        } else {
          // No Android or iOS interface found
          console.log("No native APIs found.");
        }
      }
      
      window.setUserProperty = function(name, value) {
        if (!name || !value) {
          return;
        }
      
        if (window.AnalyticsWebInterface) {
          // Call Android interface
          window.AnalyticsWebInterface.setUserProperty(name, value);
        } else if (window.webkit
            && window.webkit.messageHandlers
            && window.webkit.messageHandlers.firebase) {
          // Call iOS interface
          var message = {
            command: 'setUserProperty',
            name: name,
            value: value
         };
          window.webkit.messageHandlers.firebase.postMessage(message);
        } else {
          // No Android or iOS interface found
          console.log("No native APIs found.");
        }
      }
    } catch (error) {
      if(db){console.log("Error in CHTML JS Handler Script:", error);}
      
    }
