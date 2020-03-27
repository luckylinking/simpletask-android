Simpletask
==========
[English](./index.en.md), Ver en [Español](./index.es.md), auf [Deutsch](./index.de.md) 

[Simpletask](https://github.com/mpcjanssen/simpletask-android)基于[Gina Trapani](http://ginatrapani.org/)的精彩[todo.txt](http://todotxt.com)。该应用程序的目标是提供一种工具来执行GTD，而不提供过多的选项。尽管Simpletask可以通过相当多的设置进行自定义，但默认设置应该是正常的，不需要进行任何更改。

[Simpletask](https://github.com/mpcjanssen/simpletask-android)可以用作一个非常简单的todo列表管理器，也可以用作一个更复杂的GTD操作管理器，或者[Manage Your Now](./MYN.en.md)。

扩展
----------

Simpletask支持以下todo.txt扩展:

-   截止日期：`due:YYYY-MM-DD`
-   开始/启动日期：`t:YYYY-MM-DD`
-   重复周期：`rec:\+?[0-9]+[dwmyb]`  如[这里](https://github.com/bram85/topydo/wiki/Recurrence)所描述，但有些变动：
    - 在默认情况下，Simpletask将使用完成的日期作为循环，如链接中所述。然而，如果rec包含一个加号(例如“rec:+2w”)，日期从原始截止日期或启动日期确定。
    - `rec:1b` 将在1个工作日后重复 (记忆：*b*usiness-day). 
    - 格式是由一个正则表达式描述，所以在单词的语法是' rec: '后面跟着一个可选的' + '，然后1个或更多的数字，然后跟着一个' d ' ay， ' w ' eek， ' m ' onth或' y ' ear。例如，‘rec:12d’设置了一个12天重复的任务。
- 使用标签' h:1 '指定隐藏的任务，可以在这虚拟的任务添加预定义的清单和标签，这样的清单和标签将一直可用，即使最后一个有相关标签/清单任务被从' todo.txt '删除。这些任务默认情况下不会显示，但您可以临时设置为让它们显示。

支持
-------

Simpletask使用[weblate](https://hosted.weblate.org/engage/simpletask/)进行翻译。你可以点击链接参与贡献。

-Join the chat at [![Gitter](images/gitter.png)](https://gitter.im/mpcjanssen/simpletask-android), IRC at [#simpletask on Freenode](https://webchat.freenode.net/?channels=simpletask), or Matrix at [#simpletask:matrix.org](https://matrix.to/#/#simpletask:matrix.org).

If you want to log an issue or feature request for [Simpletask](https://github.com/mpcjanssen/simpletask-android/) you can go to [the tracker](https://github.com/mpcjanssen/simpletask-android/issues). If you find Simpletask useful, you can buy the donate app (see Settings) or donate via [Paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=mpc%2ejanssen%40gmail%2ecom&lc=NL&item_name=mpcjanssen%2enl&item_number=Simpletask&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted) me some beers.

Check the menu for more help sections or click below.

- [用户界面](./ui.en.md) Help on the user interface.
- [更新记录](./changelog.zh.md)
- [Changelog](./changelog.en.md)
- [清单和标签](./listsandtags.en.md) 为什么Simpletask使用清单和标签而不是todo.txt中的场景和项目？
- [Defined intents](./intents.en.md) Intents that can be used for automating Simpletask
- [将Simpletask用于1MTD/MYN](./MYN.en.md)
- [使用 Lua](./script.en.md)









