
  (function() {
    try {
      var db = {{Debug Mode}};
      var ecommerceData = {{DLV - ecommerce}};
      var eventName = {{Event}};
  
      if (!ecommerceData || !eventName) {
        if (db) {
          console.log('Ecommerce data or event name is missing');
        }
        return;
      }
  
  
      // Prepare event parameters
      var eventParams = {
        currency: "AUD",
        value: 0,
        items: ecommerceData.items
      };
  
      // Call logEvent with the event name and parameters
      logEvent(eventName, eventParams);
  
      if (db) {
        console.log('Pushed to JS Handler', eventName, eventParams);
      }
    } catch(err) {
      if (db) {
        console.log('Error in ecommerce tracking:', err);
      }
    }
  })();

  