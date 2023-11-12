;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Readme
;
;	these macros will ONLY work if:
;		- you're using a 1920x1080 monitor
;		- you play in WINDOWED fullscreen mode (Fullscreen unchecked in video settings)
;		- your minecraft's gui scaling is set to 2
;
;	SOME macros will ONLY work if:
;		- you have a home named fjarm that points at your bed
;		- you play on survivalquest and have access to at least 3 vaults, & server warps
;		- your third vault is configured with
;			- a fishing rod in the 19th slot
;			- your pickaxes in the 20th slot
;			- your axe/sword in the 21st slot
;			- your shovel/hoe in the 22nd slot
;		- your first, second, and third slots in your hotbar are pickaxe, sword/axe, and shovel/hoe
;
;	Obviously these are for my own personal use, with these very specific requirements *down to the sethome name*
;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Table of Contents

;______________________________
;  readme					   \
;  table of contents (meta)		|
;  auto execute					|- header
;  debug functions				|
;______________________________/

;______________________________
;  misc misc functions		   \
;  color						|
;  wait for gui					|
;  								|- misc functions
;  send commands				|
;  sprumping (sprint jumping)	|
;______________________________/

;
;  spam click
;	  left: 	alt+G ; this isn't even like, clever, this is just some mfers name that happened to also be two keys next to each other
;	  right: 	alt+H
;	  
;  ctrl+alt macros
;	  sprump: 	ctrl+alt+W		(manually sprump to cancel)
;	  rtp: 		ctrl+alt+R 		(/spawn and sprump into rtp portal)
;	  sleep: 	ctrl+alt+S 		(/home fjarm & right click bed)
;	  fish: 	ctrl+alt+F 		(/sw swamp & equip rod)
;  
;  alt macros
;  	  /vault: 	alt+V, <1-9>
;	  /sp: 		alt+P, <1-5 OR D> (1-4 for weeks 1-4, 5/D for dailies)
;  
;	  simple commands
;		  /condense: 		alt+C
;		  /warp greenshop:	alt+X
;		  /back:			alt+B
;		  /ec:				alt+E
;
;	  quick swaps
;		  silk/fort pick:	alt+1
;		  axe/sword: 		alt+2
;		  shovel/hoe: 		alt+3

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; auto execute / header

#MaxThreadsPerHotkey 4

oldClipBoard := ""
temp := ""
thisColor := 0x000000

#Persistent

SetTimer, spamLClick, 50
SetTimer, spamLClickSlow, 1500
SetTimer, spamRClick, 50

checkDurabilityToggle := false

return ; this line to prevents auto-execute section from interfering with shit

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; end auto execute

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; debug functions


^!5::Reload

^!0::
	startSprumping()
	
	sleep 400
	
	Loop 35{
		SendMouse_RelativeMove(-12, 6)
		sleep 15
	}
	stopSprumping()
	
	click right
return


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; misc functions

; this wrapper is stolen as hell lmao, I don't need to know how dll calls work in AHK
; used for mouse movement in game, outside of GUI
SendMouse_RelativeMove(x, y) { ; send fast relative mouse moves
	DllCall("mouse_event", "UInt", 0x01, "UInt", x, "UInt", y) ; move
}

RandSleep(x,y) { ; https://www.autohotkey.com/board/topic/66115-random-sleep-times/
	Random, rand, %x%, %y%
	Sleep %rand%
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; color

getMouseColor(){
	MouseGetPos X, Y 
	PixelGetColor, Color, %X%, %Y%, RGB
	return Color
}

pixelEqualsColor(xPos, yPos, checkingColor) {
	thisColor := 0x000000
    PixelGetColor, thisColor, %xPos%, %yPos%, RGB
    if (thisColor == checkingColor) {
        return true
    }
    return false
}

waitForColor(xPos, yPos, checkingColor){
	Loop 500{
		if(pixelEqualsColor(xPos, yPos, checkingColor)){
			break
		}
		sleep 16
	}
}

colorsAreSimilar(color1, color2, thresholdPercent){
	threshold := thresholdPercent*((256*3)/100)
	
	; MsgBox, ColorsAreSimilar: color 1: %color1% color 2: %color2% thresholdPercent: %thresholdPercent% threshold: %threshold%

	r := format("{:d}","0x" . substr(color1,3,2))
	g := format("{:d}","0x" . substr(color1,5,2))
	b := format("{:d}","0x" . substr(color1,7,2))
	
	rr := format("{:d}","0x" . substr(color2,3,2))
	gg := format("{:d}","0x" . substr(color2,5,2))
	bb := format("{:d}","0x" . substr(color2,7,2))
	
	; MsgBox, ColorsAreSimilar: r: %r% g: %g% b: %b% rr: %rr% gg: %gg% bb: %bb%
	
	diffR := Abs(r-rr)
	diffG := Abs(g-gg)
	diffB := Abs(b-bb)
	
	; MsgBox, ColorsAreSimilar: diffR: %diffR% diffG: %diffG% diffB: %diffB%
	
	difference := diffR+diffG+diffB
	
	if(difference < threshold){
		return true
	}
	return false
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; wait for gui

waitForGui(){
	waitForColor(800, 559, 0xC6C6C6)
}

waitForCanGui(){
	sendCommand("ec")
	
	send {esc}
	waitForGui()
	if(pixelEqualsColor(800, 559, 0xC6C6C6)){
		send e
		sleep 50
		return
	}
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; send commands

sendCommandStart(command){
	oldClipBoard := clipboard
	
	send /
	clipboard := command
	sleep 150
	Send ^v
	sleep 150
}

sendCommandSend(){
	send {enter}
	clipboard := oldClipBoard
}

sendCommand(command){
	sendCommandStart(command)
	sendCommandSend()
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; sprumping

startSprumping(){
	cycle := true
	sleep 500

	Send {LControl down}
	Send {space down}
	
	Loop 50, 
	{
		if(cycle == false){
			return
		}
		Send {w down}
	}
}

stopSprumping(){
	Send {LControl up}
	Send {space up}
	Send {w up}
	cycle := false
	sleep 100
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; spam click

spamLClick:
    If (!toggleSpamClickL)
        Return
    Click
return

spamLClickSlow:
    If (!toggleSpamClickLSlow)
        Return
		
	;RandSleep(100, 500)
	
	;click, right
	;waitForGui()
	;click, right
	;SendMouse_RelativeMove(37, 0)
	;sleep 100
	;click, right
	;sleep 100
	;send {esc}
	
	
	;old: 
    Click
return

spamRClick:
    If (!toggleSpamClickR)
        Return
    Click right
return

!g::
    toggleSpamClickL := !toggleSpamClickL ; bind alt+g and alt+h to spam L & r click functions & start loop
return

^!g::
    toggleSpamClickLSlow := !toggleSpamClickLSlow ; bind alt+g and alt+h to spam L & r click functions & start loop
return

!h::
    toggleSpamClickR := !toggleSpamClickR
return

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; displayer tests

; /display create [block (defaults to item)] [atSelected]
testDisplayCreate1() {
    sendCommand("give brewingstand 64")
    sleep 100

    sendCommand("display create")
    send {w down}
    sleep 500
    send {w up}

    sendCommand("display create block")
    send {w down}
    sleep 500
    send {w up}
}

testDisplayClosest() {
	sendCommand("display closest")
}

testDisplayCreate2() {
    sleep 100

    sendCommand("display create item atSelected")
    send {w down}
    sleep 500
    send {w up}

    sendCommand("display create block atSelected")
    send {w down}
    sleep 500
    send {w up}
}

testDisplayNearby() {
	sendCommand("display nearby")
}

testDisplayGui() {
	sendCommand("display gui")
	sleep 1000
	send {esc}
}

testAdvDisplay() {
	sendCommand("advdisplay setsize 3")
	sendCommand("advdisplay changeSize -1")
	sendCommand("advdisplay setrotation 20 20")
	sendCommand("advdisplay changerotation -65 -65")
	sendCommand("advdisplay changeposition 0 3 0")
	sendCommand("advdisplay rename william")
	sendCommand("say should have size of 2, rotation -45 -45, position shifted up 3, and be named william")
	sendCommand("advdisplay details")
	
	
	send {w down}
    sleep 500
    send {w up}

    sleep 100

    sendCommand("display create")
	sleep 100
	sendCommand("displaygroup parent william")
	sendCommand("say should be a child of william")
	sendCommand("advdisplay details")
	
	sendCommand("displaygroup unparent")
	sendCommand("say should not be a child")
	sendCommand("advdisplay details")
}

testDisplayDestroy() {
	send {w down}
	sleep 500
	sleep 500
	send {w up}
    sleep 100

    sendCommand("display create")
	sendCommand("advdisplay rename balls")
	sendCommand("advdisplay details")
	sendCommand("display destroy")
	sendCommand("advdisplay details")
}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ctrl+alt macros


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; sprump

^!W:: ; bind alt+ctrl+w to hold w, ctrl, and space (sprint jump to cancel)
	startSprumping()
return

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; test displayer

^!t:: ; bind ctrl+alt+t to test displayer
	; /clear +inventory
	sendCommand("cmi clear GreensUsername -confirmed +inventory")

	sendCommand("home displayertest")

    send 1

    testDisplayCreate1()
	sleep 500
	testDisplayClosest()
	sleep 500
	testDisplayCreate2()
	sleep 500
	testDisplayNearby()
	sleep 500
	testDisplayGui()
	sleep 500
	testAdvDisplay()
	sleep 500
	testDisplayDestroy()
	sleep 500
	sendCommand("display help")
return