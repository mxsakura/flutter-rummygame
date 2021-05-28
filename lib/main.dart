import 'package:adjust_sdk/adjust.dart';
import 'package:adjust_sdk/adjust_config.dart';
import 'package:adjust_sdk/adjust_event.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_swiper/flutter_swiper.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';

import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_rummygame/pages/details.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    AdjustConfig config = new AdjustConfig('k1ocmqx4zke8', AdjustEnvironment.sandbox);
    Adjust.start(config);
    print('Adjust初始化成功');
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          backgroundColor: Color.fromRGBO(255, 127, 0, 1.0),
          title: Container(
            alignment: Alignment.center,
            child: Text('HOME'),
          ),
        ),
        body: Home_page(),
      ),
      routes: {
        //路由注册
        '/jump': (context) => AppDetails(),
      },
    );
  }
}

class People {
  String name;
  int age;

  People(this.name, this.age);
}

class Home_page extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => RequestSwiper();
}

class RequestSwiper extends State<Home_page> {
  String BaseURL = 'https://s.t3uel30.com/page';
  List banners = [];
  List recommended = [];
  List hotgames = [];

  @override
  initState() {
    // TODO: implement initState
    super.initState();
    getBanners();
  }

  getBanners() async {
    var response = await http.get("https://s.t3uel30.com/page/app.json");
    if (response.statusCode == 200) {
      Map map = json.decode(response.body);

      setState(() {
        banners = map["banner"];
        recommended = map["games"].take(3).toList();
        hotgames = map["games"].skip(3).toList();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (banners.length == 0) {
      return LoadingPage();
    } else {
      return ListView(
        padding: EdgeInsets.all(20),
        children: [
          Container(
            height: 200,
            child: new Swiper(
              itemBuilder: (BuildContext context, int index) {
                return CachedNetworkImage(
                  imageUrl: BaseURL + banners[index]["image"],
                  placeholder: (context, url) => SpinKitFadingCircle(
                    color: Color.fromRGBO(255, 127, 0, 1.0),
                    size: 30.0,
                  ),
                  fit: BoxFit.fill,
                );
              },
              pagination: new SwiperPagination(),
              itemCount: banners.length,
              viewportFraction: 0.8,
              scale: 0.9,
              autoplay: true,
              onTap: (index) {
                print(index);
              },
            ),
          ),
          Container(
            margin: EdgeInsets.fromLTRB(0, 20, 0, 20),
            alignment: Alignment.center,
            child: Text(
              'Recommended',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            crossAxisAlignment: CrossAxisAlignment.end,
            //添加数字索引，同时访问key和value
            children: recommended.map<Widget>((item) {
              return InkWell(
                child: Container(
                  width: 75,
                  child: Column(
                    children: [
                      Container(
                        margin: EdgeInsets.only(bottom: 10),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(18.75),
                          child: CachedNetworkImage(
                            imageUrl: BaseURL + item["icon"],
                            placeholder: (context, url) => SpinKitFadingCircle(
                              color: Color.fromRGBO(255, 127, 0, 1.0),
                              size: 30.0,
                            ),
                            fit: BoxFit.fill,
                            height: 75,
                          ),
                        ),
                      ),
                      Container(
                        child: Text(
                          item["name"],
                          style: TextStyle(
                            fontSize: 16,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Container(
                        child: Text(
                          item["applied"] + ' applied',
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.grey,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      )
                    ],
                  ),
                ),
                onTap: () {
                  print('recommended');
                  AdjustEvent adjustEvent = new AdjustEvent(item["trackEvent"]);
                  Adjust.trackEvent(adjustEvent);
                  print(item["trackEvent"] + '事件跟踪成功');
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => AppDetails(
                        game: item,
                      ),
                    ),
                  );
                },
              );
            }).toList(),
          ),
          Container(
            alignment: Alignment.center,
            margin: EdgeInsets.fromLTRB(0, 20, 0, 20),
            child: Text(
              'Hot Games',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Column(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            crossAxisAlignment: CrossAxisAlignment.start,
            //添加数字索引，同时访问key和value
            children: hotgames.map<Widget>((item) {
              return Container(
                  margin: EdgeInsets.only(bottom: 20),
                  child: InkWell(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Container(
                          margin: EdgeInsets.only(bottom: 10),
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(18.75),
                            child: CachedNetworkImage(
                              imageUrl: BaseURL + item["icon"],
                              placeholder: (context, url) =>
                                  SpinKitFadingCircle(
                                color: Color.fromRGBO(255, 127, 0, 1.0),
                                size: 30.0,
                              ),
                              fit: BoxFit.fill,
                              width: 75,
                              height: 75,
                            ),
                          ),
                        ),
                        Container(
                            width: 250,
                            margin: EdgeInsets.only(left: 20, top: 10),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  child: Text(
                                    item["name"],
                                    style: TextStyle(
                                      fontSize: 16,
                                    ),
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                Container(
                                  margin: EdgeInsets.only(top: 10),
                                  child: Text(
                                    item["applied"] + ' applied',
                                    style: TextStyle(
                                      fontSize: 16,
                                      color: Colors.grey,
                                    ),
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                )
                              ],
                            ))
                      ],
                    ),
                    onTap: () {
                      print('hot game');
                      AdjustEvent adjustEvent =
                          new AdjustEvent(item["trackEvent"]);
                      Adjust.trackEvent(adjustEvent);
                      print(item["trackEvent"] + '事件跟踪成功');
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => AppDetails(
                            game: item,
                          ),
                        ),
                      );
                    },
                  ));
            }).toList(),
          )
        ],
      );
    }
  }
}

class LoadingPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        new Padding(
          padding: new EdgeInsets.fromLTRB(0.0, 0.0, 0.0, 35.0),
          child: new Center(
            child: SpinKitFadingCircle(
              color: Color.fromRGBO(255, 127, 0, 1.0),
              size: 30.0,
            ),
          ),
        ),
        new Padding(
          padding: new EdgeInsets.fromLTRB(0.0, 35.0, 0.0, 0.0),
          child: new Center(
            child: new Text('loading'),
          ),
        ),
      ],
    );
  }
}
