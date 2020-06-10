# Apple-of-My-IAP

![build](https://github.com/meetup/apple-of-my-iap/workflows/build/badge.svg)

Apple-of-My-IAP is an open sourced tool and library to help developers integrate subscription based Apple In-App Purchases into their backend. 

This project is made up of two separate sub-projects:

1. A mock service which simulates Apple's iTunes IAP service
2. A scala client for interacting with both the iTunes Sandbox/Production service or the mock service

To learn more about why this project exists, see this blog post: http://making.meetup.com/post/127718510507/apple-in-app-purchase-mock-service-fake-it-till

Mock Service
------------
The IAP mock service is a standalone web application built with Scala, AngularJS, Bootstrap, and the Unfiltered routing framework (http://unfiltered.databinder.net/Unfiltered.html). 

As of right now, the most straightforward way to run the web application is from the sbt console, of which a plain vanilla installation will do (sbt installation instructions: http://www.scala-sbt.org/release/tutorial/Setup.html).

Once you have sbt installed, navigate to the root directory of the project and start the sbt console by typing ```sbt```.
Next set the project to iap-service by typing ```project iap-service```.
To run the mock service, type ```run``` into the console and hit enter. 
After you see the following message ```Embedded server running on port 9090. Press any key to stop.```, you can navigate to http://localhost:9090/ in your browser and voila! You'll see an interface for creating subscriptions in the mock service. 

You'll notice that there is a drop down menu to the right of the title "IAP Apple Mock Service" which appears to be empty. You'll also notice that the 'Create' button seems to do absolutely nothing. This is because we need to populate some plan data with which to create subscriptions. 

After starting up the mock service for the first time, an empty ```iap-service/tmp/plans.json``` file is created. To create a plan, simple edit the contents of ```plans.json``` using the following structure:

```
[
    {
      "name":"test",
      "description":"test_plan",
      "billInterval":3,
      "billIntervalUnit":"minutes",
      "trialInterval":0,
      "trialIntervalUnit":"minutes",
      "productId":"test_apple_plan"
    },
    {
      "name":"apple_ultd_cuba_1mo",
      "description":"Unilmited plan for CUBA billed monthly",
      "billInterval":1,
      "billIntervalUnit":"months",
      "trialInterval":0,
      "trialIntervalUnit":"days",
      "productId":"com.meetup.org_plan.unlimited.cuba.1mo"
    }
]
```

Each item in the list corresponds to a possible plan with which to create a subscription. After you have populated ```plans.json```, simply restart the server to see the correct information populated. 

Note: The mock service does not renew subscriptions automatically. Instead, hit the "Renew" button every time you want to renew a subscription.

###Mock Service Response
To retrieve information about a specific subscription, make a post request to the ```/verifyReceipt``` endpoint with the subscriber's receipt token as one of the parameters.

e.g. ```curl -d '{"receipt-data":"apple_ultd_cuba_1mo_4dde99c7-ff21"}' http://localhost:9090/verifyReceipt```

The mock server will then return a response containing information about the item which was purchased, and the expiration date of said purchase. The following response shows the initial purchase and one successful renewal:

```
{
  "status": 0,
  "latest_receipt_info": [
    {
      "quantity": "1",
      "product_id": "com.meetup.org_plan.unlimited.cuba.1mo",
      "transaction_id": "com.meetup.org_plan.unlimited.cuba.1mo-2015-08-17T17:54:18.085-04:00",
      "original_transaction_id": "com.meetup.org_plan.unlimited.cuba.1mo-2015-08-17T17:54:18.085-04:00",
      "purchase_date": "2015-08-17 21:54:18 Etc/GMT",
      "purchase_date_ms": "1439862858000",
      "original_purchase_date": "2015-08-17 21:54:18 Etc/GMT",
      "original_purchase_date_ms": "1439862858000",
      "expires_date": "2015-09-17 21:54:18 Etc/GMT",
      "expires_date_ms": "1442541258000",
      "is_trial_period": false
    },
    {
      "quantity": "1",
      "product_id": "com.meetup.org_plan.unlimited.cuba.1mo",
      "transaction_id": "com.meetup.org_plan.unlimited.cuba.1mo-2015-08-17T17:54:20.497-04:00",
      "original_transaction_id": "com.meetup.org_plan.unlimited.cuba.1mo-2015-08-17T17:54:18.085-04:00",
      "purchase_date": "2015-08-17 21:54:20 Etc/GMT",
      "purchase_date_ms": "1439862860000",
      "original_purchase_date": "2015-08-17 21:54:18 Etc/GMT",
      "original_purchase_date_ms": "1439862858000",
      "expires_date": "2015-09-17 21:54:20 Etc/GMT",
      "expires_date_ms": "1442541260000",
      "is_trial_period": false
    }
  ],
  "latest_receipt": "apple_ultd_cuba_1mo_66103721-d779-001"
}
```

This is a slimmed down version of what you would actually receive from Apple's IAP servers. We took out non-pertinent fields simply for the sake of reducing noise for developing. To see an example of an actual IAP response from Apple, scroll down to the bottom of this document. If there is a field that you would like included, please open an issue, or preferably, an issue and a pull request!


To make interacting with Apple and the mock service easier, we've created a client that will return a typed response (See Scala Apple Client below).


###Revolver
The mock service is outfitted with a neat plugin called Revolver, which allows access to the sbt console while the mock service is running (https://github.com/spray/sbt-revolver). To use this plugin, simply type ```re-start``` instead of the ```run``` command when starting up the mock service. 


Scala Apple Client
------------------
To get started, import the jar for the iap-api project into your project (not yet released). 

Next, create an instance of the Client based on your needs:

```
val myITunesPwd = <your Itunes account password>
val clientLogger = Logger.getLogger(Foo.class)
```

To interact with Apple's production IAP environment - 

```val client = Client.live(password = myITunesPwd, logger = clientLogger)```

To interact with Apple's sandbox IAP environment - 

```val client = Client.sandbox(password = myItunesPwd, logger = clientLogger)```

To interact with the mock IAP environment - 

```val client = Client.other(<http://url/to/mock/service>, logger = clientLogger)```

You then make calls to the IAP environment by calling - 

```client.verifyReceipt(receipt, logResponse)``` 

where ```receipt``` is the base64 encoded receipt string, and logResponse is a boolean indicating whether or not you want the IAP response logged or not. 

```verifyReceipt``` returns a Future[Either[IAPError, ReceiptResponse]. 


EXAMPLE IAP Response
====================
The following IAP Response shows a purchase and 1 successful renewal.

```
{
    "status": 0,
    "environment": "Sandbox",
    "receipt": {
        "receiptType": "ProductionSandbox",
        "adamId": 0,
        "appItemId": 0,
        "bundleId": "com.meetup.iphone",
        "applicationVersion": "409000",
        "downloadId": 0,
        "versionExternalIdentifier": 0,
        "requestDate": "2015-04-23 23:33:34 Etc/GMT",
        "requestDateMs": "1429832014510",
        "requestDatePst": "2015-04-23 16:33:34 America/Los_Angeles",
        "originalPurchaseDate": "2013-08-01 07:00:00 Etc/GMT",
        "originalPurchaseDateMs": "1375340400000",
        "originalPurchaseDatePst": "2013-08-01 00:00:00 America/Los_Angeles",
        "originalApplicationVersion": "1.0",
        "inApp": [
            {
                "quantity": "1",
                "productId": "com.meetup.iphone.test.7days",
                "transactionId": "1000000151887289",
                "originalTransactionId": "1000000151887289",
                "purchaseDate": "2015-04-15 21:46:04 Etc/GMT",
                "purchaseDateMs": "1429134364000",
                "purchaseDatePst": "2015-04-15 14:46:04 America/Los_Angeles",
                "originalPurchaseDate": "2015-04-15 21:46:06 Etc/GMT",
                "originalPurchaseDateMs": "1429134366000",
                "originalPurchaseDatePst": "2015-04-15 14:46:06 America/Los_Angeles",
                "expiresDate": "2015-04-15 21:49:04 Etc/GMT",
                "expiresDateMs": "1429134544000",
                "expiresDatePst": "2015-04-15 14:49:04 America/Los_Angeles",
                "webOrderLineItemId": "1000000029487694",
                "isTrialPeriod": "false"
            },
            {
                "quantity": "1",
                "productId": "com.meetup.iphone.test.7days",
                "transactionId": "1000000151887546",
                "originalTransactionId": "1000000151887289",
                "purchaseDate": "2015-04-15 21:49:04 Etc/GMT",
                "purchaseDateMs": "1429134544000",
                "purchaseDatePst": "2015-04-15 14:49:04 America/Los_Angeles",
                "originalPurchaseDate": "2015-04-15 21:48:06 Etc/GMT",
                "originalPurchaseDateMs": "1429134486000",
                "originalPurchaseDatePst": "2015-04-15 14:48:06 America/Los_Angeles",
                "expiresDate": "2015-04-15 21:52:04 Etc/GMT",
                "expiresDateMs": "1429134724000",
                "expiresDatePst": "2015-04-15 14:52:04 America/Los_Angeles",
                "webOrderLineItemId": "1000000029487695",
                "isTrialPeriod": "false"
            }
        ]
    },
    "latestReceiptInfo": [
        {
            "quantity": "1",
            "productId": "com.meetup.iphone.test.7days",
            "transactionId": "1000000151887289",
            "originalTransactionId": "1000000151887289",
            "purchaseDate": "2015-04-15 21:46:04 Etc/GMT",
            "purchaseDateMs": "1429134364000",
            "purchaseDatePst": "2015-04-15 14:46:04 America/Los_Angeles",
            "originalPurchaseDate": "2015-04-15 21:46:06 Etc/GMT",
            "originalPurchaseDateMs": "1429134366000",
            "originalPurchaseDatePst": "2015-04-15 14:46:06 America/Los_Angeles",
            "expiresDate": "2015-04-15 21:49:04 Etc/GMT",
            "expiresDateMs": "1429134544000",
            "expiresDatePst": "2015-04-15 14:49:04 America/Los_Angeles",
            "webOrderLineItemId": "1000000029487694",
            "isTrialPeriod": "false"
        },
        {
            "quantity": "1",
            "productId": "com.meetup.iphone.test.7days",
            "transactionId": "1000000151887546",
            "originalTransactionId": "1000000151887289",
            "purchaseDate": "2015-04-15 21:49:04 Etc/GMT",
            "purchaseDateMs": "1429134544000",
            "purchaseDatePst": "2015-04-15 14:49:04 America/Los_Angeles",
            "originalPurchaseDate": "2015-04-15 21:48:06 Etc/GMT",
            "originalPurchaseDateMs": "1429134486000",
            "originalPurchaseDatePst": "2015-04-15 14:48:06 America/Los_Angeles",
            "expiresDate": "2015-04-15 21:52:04 Etc/GMT",
            "expiresDateMs": "1429134724000",
            "expiresDatePst": "2015-04-15 14:52:04 America/Los_Angeles",
            "webOrderLineItemId": "1000000029487695",
            "isTrialPeriod": "false"
        }
    ],
    "latestReceipt": "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"
}
```

