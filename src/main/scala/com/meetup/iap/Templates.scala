package com.meetup.iap

import unfiltered.response._

trait Templates {

  def page(body: scala.xml.NodeSeq) = Html(
    <html ng-app="iap">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title>Apple IAP Mock Service</title>
        <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.15/angular.min.js"></script>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css" />
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css" />
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>

        <script src="/js/app.js"></script>
        <link href="/css/app.css" type="text/css" rel="stylesheet" />
      </head>
      <body>
        <div id="container">
          {body}
        </div>
      </body>
    </html>
  )

  def index = page(<h1>test</h1>)

}
