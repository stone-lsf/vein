var Script = function () {

    //morris chart

    $(function () {
          // data stolen from http://howmanyleft.co.uk/vehicle/jaguar_'e'_type
          Morris.Line({
                          element: 'hero-area',
                          data: [
                              {period: '2010 Q1', iphone: 2666, ipad: null, itouch: 2647},
                              {period: '2010 Q2', iphone: 2778, ipad: 2294, itouch: 2441},
                              {period: '2010 Q3', iphone: 4912, ipad: 1969, itouch: 2501},
                              {period: '2010 Q4', iphone: 3767, ipad: 4597, itouch: 2689},
                              {period: '2011 Q1', iphone: 6810, ipad: 1914, itouch: 2293},
                              {period: '2011 Q2', iphone: 5670, ipad: 4293, itouch: 1881},
                              {period: '2011 Q3', iphone: 4820, ipad: 3795, itouch: 1588},
                              {period: '2011 Q4', iphone: 15073, ipad: 5967, itouch: 5175},
                              {period: '2012 Q1', iphone: 10687, ipad: 4460, itouch: 2028},
                              {period: '2012 Q2', iphone: 8432, ipad: 5713, itouch: 1791}
                          ],

                          xkey: 'period',
                          ykeys: ['iphone', 'ipad', 'itouch'],
                          labels: ['iPhone', 'iPad', 'iPod Touch'],
                          hideHover: 'auto',
                          lineWidth: 1,
                          pointSize: 5,
                          lineColors: ['#4a8bc2', '#ff6c60', '#a9d86e'],
                          fillOpacity: 0.5,
                          smooth: true
                      });

          new Morris.Line({
                              element: 'examplefirst',
                              xkey: 'year',
                              ykeys: ['value'],
                              labels: ['Value'],
                              data: [
                                  {year: '2008', value: 20},
                                  {year: '2009', value: 10},
                                  {year: '2010', value: 5},
                                  {year: '2011', value: 5},
                                  {year: '2012', value: 20}
                              ]
                          });

          $('.code-example').each(function (index, el) {
              eval($(el).text());
          });
      }
    );

    Date.prototype.Format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) {
            fmt =
                fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt =
                    fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(
                        ("" + o[k]).length)));
            }
        }
        return fmt;
    };
}();