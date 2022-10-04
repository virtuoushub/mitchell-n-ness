# mitchell-n-ness

## setup

```sh
curl -s https://get.sdkman.io | bash
```

see also:

- https://sdkman.io/install
- https://github.com/sdkman/sdkman-cli

```sh
sdk install java 17.0.4.1-tem
```

## run

### as maven exec task

- `./mvnw exec:exec` _macOS | \*nix_
- `mvnw exec:exec` _Windows_

### as fat jar

- package
  - `./mvnw clean package` _macOS | \*nix_
  - `mvnw clean package` _Windows_

#### windows | \*nix

```sh
java -jar ./target/mitchell-n-ness-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### macos

```sh
java -XstartOnFirstThread -jar ./target/mitchell-n-ness-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

## useful resources
* https://github.com/virtuoushub/awesome-gamedev
* https://github.com/virtuoushub/fuzzy-octo-shame
* https://github.com/whoa-algebraic/game-off-2016
* https://www.piskelapp.com/
* https://www.bfxr.net/
* http://drpetter.se/project_sfxr.html

## Tested Controllers
GUID | Controller Name
--- | ---
030000004c050000c405000000010000 | PS4 Controller
030000005e0400008e02000000000000 | X360 Controller
030000005e040000d102000000000000 | Xbox One Wired Controller
*(why is 030000005e0400008e02000000000000 not 03000000c6240000045d000000000000,Xbox 360 Wired Controller?)*

### game jam boilerplate
From https://itch.io/jam/game-off-2017

<div class="jam_content formatted"><figure><img src="https://user-images.githubusercontent.com/18125109/31239479-d554f29c-a9c2-11e7-8138-71483d537ca9.gif"></figure>
<p>Welcome to Game Off—our 5th annual game jam celebrating open source.&nbsp;The theme for this year’s jam is&nbsp;<strong>throwback</strong>.</p>
<p>Let your imagination run wild and interpret that in any way you like, but here are a few possible interpretations for inspiration:</p>
<ul><li>a reversion to an earlier ancestral characteristic</li><li>a person or thing having the characteristics of a former time</li><li>a nostalgia for something in the past (fashion, movies, literature, games, technology)</li><li>something retro—a blast from the past</li><li>something passé—no longer fashionable or popular</li><li>throwing something back—as in a ball or reply</li></ul>
<h2>How to participate</h2>
<ol><li>Create a game based on the theme over the next month.</li><li><a href="https://github.com/join" rel="nofollow noopener">Sign up for a free GitHub account</a>&nbsp;if you don't already have one.</li><li><a href="https://itch.io/jam/game-off-2017">Join the Game Off on itch.io</a>. If you don’t already have an itch.io account,&nbsp;<a href="https://itch.io/login?intent=jam_submit&amp;return_to=https%3A%2F%2Fitch.io%2Fjam%2Fgame-off-2017">log in with your GitHub account</a>.</li><li>Create a new repository to store the source code and any assets you’re able to share for your entry and push your changes before December 1 13:37 PDT.</li><li>Submit your game through itch.io.</li></ol>
<p>You can participate by yourself or as a team. Multiple submissions are fine. And of course, the use of open source software is encouraged.</p>
<h2>Voting</h2>
<p>This year, voting will open shortly after the jam ends and is open to everyone who’s submitted a game. There’ll be plenty of time to play and vote on the entries.</p>
<p>As always, we'll highlight some of our favorites games on the GitHub Blog, and the world will get to enjoy (and maybe even contribute to or learn from) your creations.</p>
<h2>If you're new to Git, GitHub, or version control</h2>
<ul><li><a href="https://git-scm.com/documentation" rel="nofollow noopener">Git Documentation</a>: everything you need to know about version control and how to get started with Git</li><li><a href="https://help.github.com/" rel="nofollow noopener">GitHub Help</a>: everything you need to know about GitHub</li><li>Any questions about GitHub? Please&nbsp;<a href="https://github.com/contact?form%5Bsubject%5D=GitHub%20Game%20Off" rel="nofollow noopener">contact the GitHub Support team</a>&nbsp;and they'll be delighted to help you</li></ul>
<h2>If you're new to itch.io or game development</h2>
<p>The <a href="https://itch.io/jam/game-off-2017/community">itch.io community feature</a> is enabled for this jam—that’s a great place to ask questions specific to the Game Off, share tips, etc.</p>
<p>And don’t be shy—share your progress! The official Twitter hashtag for the Game Off is&nbsp;<a href="https://twitter.com/hashtag/githubgameoff?f=tweets" rel="nofollow noopener"><code>#GitHubGameOff</code></a>.<br></p>
<p><br></p></div>

### research
#### fullscreen
- https://github.com/LWJGL/lwjgl3/blob/master/modules/core/src/test/java/org/lwjgl/demo/glfw/Gears.java#L131-L148
- https://github.com/LWJGL/lwjgl3/blob/master/modules/core/src/test/java/org/lwjgl/demo/glfw/Gears.java#L161-L164
- https://github.com/glfw/glfw/blob/3.2.1/examples/boing.c#L238-L266