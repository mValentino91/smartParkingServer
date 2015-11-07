<%-- 
    Document   : interactiveMap
    Created on : 11-dic-2014, 17.44.55
    Author     : Marco Valentino
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
    <head>
        <script type="text/javascript" 
                src = "http://maps.googleapis.com/maps/api/js?key=AIzaSyBKbphxUcFrE24FYlwrs6K-yzXBguXRhhg&sensor=true" >
        </script>
        <script>
            var map;
            var markers = new Array();

            function initInteractiveMap() {

                var mapOptions = {
                    center: new google.maps.LatLng(40.8485091, 14.25574759999995),
                    zoom: 15,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                };

                map = new google.maps.Map(document.getElementById("map"),
                        mapOptions);

            <% int i = 0;%>
            <c:forEach var = "poi" items = "${poiList}">
                markers[<%=i%>] = new google.maps.Marker({
                    position: new google.maps.LatLng(${poi.location[0]}, ${poi.location[1]}),
                    title: "${poi.name}",
                    icon: "./dist/img/${poi.zone}.png",
                    map: map});

                <%i++;%>
            </c:forEach>
            }
        </script>
    </head>
    <body onload="initInteractiveMap()">
        <!--Map
        ===================================================-->
        <div id="mapContainer">
            <div id="map" style="height: 100%; width: 100%;"></div>
        </div>
        <!--/Map
        ===================================================-->
    </body>
</html>
