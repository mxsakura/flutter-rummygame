import 'package:adjust_sdk/adjust.dart';
import 'package:adjust_sdk/adjust_event.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_spinkit/flutter_spinkit.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:video_player/video_player.dart';
import 'package:flutter_rummygame/widget/previewimg.dart';

class AppDetails extends StatefulWidget {
  Map game;

  AppDetails({Key key, this.game}) : super(key: key);

  @override
  _AppDetails createState() => _AppDetails(game: this.game);
}

class _AppDetails extends State<AppDetails> {
  Map game;
  String BaseURL = 'https://s.t3uel30.com/page';

  _AppDetails({this.game});

  VideoPlayerController _controller;

  get support => null;

  //重写类方法initState()，初始化界面
  @override
  void initState() {
    super.initState();
    var videos =
        game["media"].where((media) => media["type"] == "video").toList();
    print(videos);
    //设置视频参数 (..)是级联的意思
    if (videos.length > 0) {
      _controller = VideoPlayerController.network(BaseURL + videos[0]["url"])
        ..initialize().then((_) {
          // 确保在初始化视频后显示第一帧，直至在按下播放按钮。
          setState(() {});
        })
        ..setLooping(true)
        ..play();
    }
  }

  //打开外部浏览器
  _openBrower(String url) async {
    print(url);
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      throw 'Could not launch $url';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Color.fromRGBO(255, 127, 0, 1.0),
        title: Text(""),
        leading: InkWell(
          onTap: () {
            Navigator.pop(context);
          },
          child: Icon(
            Icons.arrow_back,
          ),
        ),
      ),
      body: ListView(
        padding: EdgeInsets.all(20),
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(18.75),
                  child: CachedNetworkImage(
                    imageUrl: BaseURL + game["icon"],
                    placeholder: (context, url) => SpinKitFadingCircle(
                      color: Color.fromRGBO(255, 127, 0, 1.0),
                      size: 30.0,
                    ),
                    fit: BoxFit.fill,
                    width: 75,
                  ),
                ),
              ),
              Container(
                  width: 250,
                  margin: EdgeInsets.only(left: 30),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        child: Text(
                          game["name"],
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      Container(
                        margin: EdgeInsets.only(top: 20),
                        child: Text(
                          game["applied"] + ' applied',
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.grey,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      )
                    ],
                  )),
            ],
          ),
          Container(
            padding: EdgeInsets.only(top: 20),
            child: ElevatedButton(
              child: Text(
                'Download',
                style: TextStyle(
                    color: Color.fromARGB(255, 255, 255, 255), fontSize: 18),
              ),
              style: ButtonStyle(
                backgroundColor: MaterialStateProperty.all(
                  Color.fromRGBO(255, 127, 0, 1.0),
                ),
              ),
              onPressed: () {
                print(game["trackEventD"]);
                AdjustEvent adjustEvent = new AdjustEvent(game["trackEventD"]);
                Adjust.trackEvent(adjustEvent);
                print(game["trackEventD"] + '事件跟踪成功');
                _openBrower(game["srcUrl"]);
              },
            ),
          ),
          Container(
            margin: EdgeInsets.only(top: 20),
            height: 200,
            child: ListView(
              scrollDirection: Axis.horizontal,
              children: [
                Row(
                  children: game["media"].asMap().keys.map<Widget>((index) {
                    if (game["media"][index]["type"] == "image") {
                      return InkWell(
                        child: Container(
                          margin: index < (game["media"].length - 1)
                              ? EdgeInsets.only(right: 10)
                              : EdgeInsets.only(right: 0),
                          child: CachedNetworkImage(
                            imageUrl: BaseURL + game["media"][index]["url"],
                            placeholder: (context, url) => SpinKitFadingCircle(
                              color: Color.fromRGBO(255, 127, 0, 1.0),
                              size: 30.0,
                            ),
                            fit: BoxFit.fill,
                          ),
                        ),
                        onTap: () {
                          var currentImg =
                              BaseURL + game["media"][index]["url"];
                          var images = game["media"]
                              .where((media) => media["type"] == "image")
                              .toList();
                          var photoList = images
                              .map((item) => BaseURL + item["url"])
                              .toList()
                              .cast<String>();
                          var initialPage = photoList.indexOf(currentImg);
                          print(photoList is List<String>);
                          print(initialPage);
                          Navigator.of(context).push(PageRouteBuilder(
                              pageBuilder: (c, a, s) => PreviewImagesWidget(
                                    photoList,
                                    initialPage: initialPage,
                                  )));
                        },
                      );
                    } else {
                      return Container(
                        margin: index < (game["media"].length - 1)
                            ? EdgeInsets.only(right: 10)
                            : EdgeInsets.only(right: 0),
                        child: _controller.value.initialized
                            ? AspectRatio(
                                aspectRatio: _controller.value.aspectRatio,
                                child: VideoPlayer(_controller),
                              )

                            //如果视频没有加载好或者因网络原因加载不出来则返回Container 组件
                            //一般用于放置过渡画面
                            : Container(
                                child: LoadingPage(),
                              ),
                      );
                    }
                  }).toList(),
                )
              ],
            ),
          ),
          Container(
            margin: EdgeInsets.fromLTRB(0, 20, 0, 20),
            child: Text(
              'Details',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Container(
            child: Text(
              game["details"],
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey,
              ),
            ),
          )
        ],
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    var videos =
        game["media"].where((media) => media["type"] == "video").toList();
    if (videos.length > 0) {
      _controller.dispose();
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
