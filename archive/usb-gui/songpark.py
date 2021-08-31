#!/usr/bin/env python
# -*- coding: utf-8 -*-
try:
	from tkinter import *
	from tkinter.filedialog import asksaveasfilename,askopenfilename
	from tkinter.messagebox import *
except:
	from Tkinter import *
	from tkFileDialog import asksaveasfilename,askopenfilename
	from tkMessageBox import *
from serial import Serial
from serial.tools.list_ports import comports
import time, sys, json
from os.path import expanduser,isfile

def _(text):
	return text

upDwnRow =7
btnCol = 0
btnFcol = 1

class Application(Tk):
	def onCopy(self):
		self.text.event_generate("<<Copy>>")
	def onPaste(self):
		self.text.event_generate("<<Paste>>")
	def onCut(self):
		self.text.event_generate("<<Cut>>")
	def onClear(self):
		self.text.delete('1.0', END)
	def onClick(self, event=None):
		showinfo("Songpark Teleporter","The future of live entertainment!\n")
	def onOpen(self):
		filename = askopenfilename(filetypes = ((_("Text files"), "*.txt"),(_("All files"), "*.*") ))
		if filename == "":
			return
		self.text.delete('1.0', END)
		fd = open(filename,"r")
		for line in fd:
			self.text.insert(INSERT, line)
		fd.close()
	def onSave(self):
		filename = asksaveasfilename(filetypes = ((_("Text files"), "*.txt"),(_("All files"), "*.*") ))
		if filename == "":
			print("No file name given")
			return
		fd = open(filename,"w")
		print("File ::%s saved"%filename)
		fd.write(self.text.get('1.0', END))
		fd.close()
	def openPort(self):
		if self.serial == None:
			self.serial = Serial(port=self.in_sport.get(), baudrate=self.baud.get())
			#self.serial.open()
		else:
			self.serial.close()
			self.serial = Serial(port=self.in_sport.get(), baudrate=self.baud.get())
			#self.serial.open()
		self.text.insert(INSERT, "===========Port %s opened>>>\n"%self.in_sport.get(),"info")
		self.Reciver()
	def closePort(self):
		if self.serial != None:
			self.serial.close()
		#self.port.set("")
		self.serial = None
		self.text.insert(INSERT, "===========Port %s closed>>>\n"%self.in_sport.get(),"info")
	def setBaud(self):
		if self.serial != None:
			self.serial.close()
			self.serial = Serial(port=self.in_sport.get(), baudrate=self.baud.get())
			self.serial.open()
	def OnEnterText(self, event):
		text = u''+self.text.get("end-1c linestart","end-1c")
		self.text.delete("end-1c linestart","end")
		self.Send(text)
	def OnEnterInput(self, event):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			text=u''+self.input.get()+"\n"
			self.Send(text)
			self.input.delete(0, END)
		
	#---------------------------------------------------------------------------------------------------------
	def reboot(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("reboot now\n")
	def shutdown(self):
		self.Send("shutdown now\n")
	def engineStart(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
	# 	    self.Send("cantavi_streamer "+"02.00.00.00.00.20 " + "10.0.0.10 " + "217.118.44.201 " + "10.0.0.138 " + "255.255.255.0 " + "7180 " + "7170\n")
			self.Send("cantavi_streamer "+self.in_mac.get() +" "+ self.in_local_ip.get() +" "+ self.in_dest_ip.get() +" "+ self.in_gw_ip.get() +" "+ self.in_netmask.get() +" "+ self.in_dest_port.get() +" "+ self.in_sync_port.get()+"\n")
	
	def engineStop(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("exit\n")
	def volume5(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("vgg 5\n")
	def volume10(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("vgg 10\n")
	def volume15(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("vgg 15\n")
			
	def volumeUp(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.vol += 5
			if self.vol > 30 :
				self.vol = 30			
			self.Send("vgg %d\n"%self.vol)
	def volumeDown(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.vol -= 5
			if self.vol < 0 :
				self.vol = 0
			self.Send("vgg %d\n"%self.vol)
			
			
	def netVolUp(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.netvol += 5
			if self.netvol > 30 :
				self.netvol = 30			
			self.Send("vnl %d\n"%self.netvol)
			self.Send("vnr %d\n"%self.netvol)
	def netVolDown(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.netvol -= 5
			if self.netvol < 0 :
				self.netvol = 0
			self.Send("vnl %d\n"%self.netvol)
			self.Send("vnr %d\n"%self.netvol)
	def strstart(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("strstart\n")
	def strstop(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("strstop\n")
	def reset(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("dreset\n")
	def delay5(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("setpoutdly 5\n")
	def delay10(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("setpoutdly 10\n")
	def delay15(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("setpoutdly 5\n")
			
	def delayUp(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR::Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.delay += 1
# 			if self.delay > 30 :
# 				self.delay = 30
			self.Send("setpoutdly %d\n"%self.delay)
	def delayDown(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.delay -= 1
			if self.delay < 0 :
				self.delay = 0
				
			self.Send("setpoutdly %d\n"%self.delay)
	def measure(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("msuredly\n")
	def syncon(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("setsyncen\n")
	def syncoff(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("setsyncoff\n")
	def latency(self):
		if self.serial == None:
			self.text.insert(INSERT, "ERROR:: Port %s is not open !!!!!!!!\n"%self.in_sport.get(),"error")
			return
		else:
			self.Send("msuredly\n")
	
	
	def resize(self, event):	
		#print("The value of H is::",self.h)	   
		pixelX=self.winfo_width()-self.vscrollbar.winfo_width()
		pixelY=self.winfo_height()
		self.text["width"]=int(round(pixelX/(self.h[1]*1.0))) 
		self.text["height"]=int(round(pixelY/(self.h[0]*1.8)))
	#--------------------------------------------------------------------------------------------------------
		
		
	def LocalEcho(self, text):
		if self.echo.get():
			if self.dir.get(): self.text.insert(INSERT, "<<< ", "out")
			self.text.insert(INSERT, "%s\n"%text)
			if(self.autoscroll): self.text.see("end")
	def Send(self, text):
		data = ""
		self.LocalEcho(text)
		if self.sendhex.get():
			for c in text.split(" "):
				data += "%c"%int(c,16)
		else:
			data = text
		data += self.endline.get().replace(" ","")
		if self.serial != None:
			try:
				self.serial.write(bytes(data,encoding='utf8'))
			except:#Python2
				self.serial.write(bytes(data))
				
	def Reciver(self):
		if self.serial != None:
			rx = []
			while (self.serial.inWaiting()>0):
				rx.append(ord(self.serial.read(1)))
				time.sleep(0.001)
			if rx != []:
				if self.dir.get(): self.text.insert(INSERT, ">>> ", "in")
				for s in rx:
# 					if self.hexmode.get():
# 						self.text.insert(INSERT, "%02X "%s)
# 					else:
					self.text.insert(INSERT, "%c"%s)
				if(rx[-1] != 0x0d): self.text.insert(INSERT, "\n")
				if(self.autoscroll): self.text.see("end")
		self.after(1, self.Reciver)
	def onExit(self):
		if askyesno(_("Exit"), _("Do you want to quit the application?")):
			self.closePort()
			self.saveConfig()
			
			self.destroy()
	def setDefaults(self):
		config = {}
		config['endline'] = " "
		config['port'] = "/dev/ttyACM0"
		config['baud'] = 115200
		config['echo'] = True
		config['hexmode'] = False
		config['dir'] = True
		config['autoscroll'] = True
		config['sendhex'] = False
		config['local_mac'] = "02.00.00.00.02.02"
		config['local_ip'] = "10.0.10.11"
		config['dest_port'] = "7071"
		config['dest_ip'] = "10.0.10.21"
		config['sync_port'] = "7022"	
		config['gw_ip'] = "10.0.10.1"
		config['netmask_ip'] = "255.255.255.0"
		
		return config
	def saveConfig(self):
		config = {}
		config['endline'] = self.endline.get()
		config['port'] = self.in_sport.get()
		config['baud'] = self.baud.get()
		config['echo'] = self.echo.get()
		config['hexmode'] = self.hexmode.get()
		config['dir'] = self.dir.get()
		config['autoscroll'] = self.autoscroll.get()
		config['sendhex'] = self.sendhex.get()
		config['local_mac'] = self.in_mac.get()
		config['local_ip'] = self.in_local_ip.get()
		config['dest_port'] = self.in_dest_port.get()
		config['dest_ip'] = self.in_dest_ip.get()
		config['sync_port'] = self.in_sync_port.get()		
		config['gw_ip'] = self.in_gw_ip.get()
		config['netmask_ip'] = self.in_netmask.get()
		
		
		cfg = open(expanduser("~/.serialterminal.json"),"w")
		cfg.write(json.dumps(config, indent = 4))
		cfg.close()
	def createWidgets(self):
		config = self.setDefaults()
		if isfile(expanduser("~/.serialterminal.json")):
			try:
				config = json.loads(open(expanduser("~/.serialterminal.json")).read())
			except:
				pass
		self.serial = None
		self.endline = StringVar()
		self.endline.set(config['endline'])
		

		self.in_sport = StringVar()
		self.in_mac = StringVar()
		self.in_local_ip = StringVar()
		self.in_dest_ip = StringVar()
		self.in_dest_port = StringVar()
		self.in_sync_port = StringVar()
		self.in_gw_ip = StringVar()
		self.in_netmask = StringVar()
	
		self.in_sport.set(config['port'])
		self.in_mac.set(config['local_mac'])
		self.in_local_ip.set(config['local_ip'])
		self.in_dest_ip.set(config['dest_ip'])
		self.in_dest_port.set(config['dest_port'])
		self.in_sync_port.set(config['sync_port'])
		self.in_gw_ip.set(config['gw_ip'])
		self.in_netmask.set(config['netmask_ip'])
		
		self.delay = 5
		self.vol = 0
		self.netvol = 30

		self.inline = StringVar()
		#self.port = StringVar()
		#self.port.set(config['port'])
		self.baud = IntVar()
		self.baud.set(config['baud'])
		self.echo = IntVar()
		self.echo.set(config['echo'])
		self.hexmode = IntVar()
		self.hexmode.set(config['hexmode'])
		self.dir = IntVar()
		self.dir.set(config['dir'])
		self.autoscroll = IntVar()
		self.autoscroll.set(config['autoscroll'])
		self.sendhex = IntVar()
		self.sendhex.set(config['sendhex'])
		self.menubar = Menu(self)
		filemenu = Menu(self.menubar, tearoff=0)
		filemenu.add_command(label=_("Open"), command=self.onOpen)
		filemenu.add_command(label=_("Save"), command=self.onSave)
		filemenu.add_separator()
		filemenu.add_command(label=_("Exit"), command=self.quit)
		self.menubar.add_cascade(label=_("File"), menu=filemenu)
		editmenu = Menu(self.menubar, tearoff=0)
		editmenu.add_command(label=_("Cut"), command=self.onCut)
		editmenu.add_command(label=_("Copy"), command=self.onCopy)
		editmenu.add_command(label=_("Paste"), command=self.onPaste)
		editmenu.add_separator()
		editmenu.add_command(label=_("Clear"), command=self.onClear)
		self.menubar.add_cascade(label=_("Edit"), menu=editmenu)
		portmenu = Menu(self.menubar, tearoff=0)
		for n, (port, desc, hwid) in enumerate(sorted(comports()), 1):
			sys.stderr.write('--- {:2}: {:20} {!r}\n'.format(n, port, desc))
			portmenu.add_radiobutton(label=port , value=port, variable=self.in_sport, command=self.openPort)
		portmenu.add_separator()
# 		portmenu.add_checkbutton(label=_("Output in HEX"), onvalue=True, offvalue=False, variable=self.hexmode)
# 		portmenu.add_checkbutton(label=_("Input in HEX"), onvalue=True, offvalue=False, variable=self.sendhex)
		portmenu.add_checkbutton(label=_("Local echo"), onvalue=True, offvalue=False, variable=self.echo)
		portmenu.add_checkbutton(label=_("Direction tag"), onvalue=True, offvalue=False, variable=self.dir)
		portmenu.add_checkbutton(label=_("Autoscroll"), onvalue=True, offvalue=False, variable=self.autoscroll)
		baudmenu=Menu(portmenu, tearoff=0)
		for n in [1200,2400,4800,9600,14400,38400,57600,115200]:
			baudmenu.add_radiobutton(label=str(n), value=n, variable=self.baud, command=self.setBaud)
		portmenu.add_cascade(label=_("Baudrate"), menu=baudmenu)
		endmenu=Menu(portmenu, tearoff=0)
		endmenu.add_radiobutton(label=_("None"), value=" ", variable=self.endline)
		endmenu.add_radiobutton(label="0x0A - LF", value="\n", variable=self.endline)
		endmenu.add_radiobutton(label="0x0D - CR", value="\r", variable=self.endline)
		endmenu.add_radiobutton(label="CR+LF", value="\r\n", variable=self.endline)
		endmenu.add_radiobutton(label="TAB", value="\t", variable=self.endline)
		portmenu.add_cascade(label=_("End of line"), menu=endmenu)
		portmenu.add_separator()
		portmenu.add_command(label=_("Close Port"), command=self.closePort)
		self.menubar.add_cascade(label=_("Port"), menu=portmenu)
		helpmenu = Menu(self.menubar, tearoff=0)
		helpmenu.add_command(label=_("About"), command=self.onClick)
		self.menubar.add_cascade(label=_("Help"), menu=helpmenu)
		self.config(menu=self.menubar)
		
		
		self.topFrame = Frame(self, borderwidth=2, relief="ridge",width=self.dwidth-20, height = self.dheight/6)
		self.topFrame.grid(row=0, column=0, columnspan=3, padx=10, pady=2)
		
		self.leftFrame = Frame(self, borderwidth=2, relief="ridge",width=self.dwidth/16, height = self.dheight-20)
		self.leftFrame.grid(row=1, column=0, padx=10, pady=2)
		self.rightFrame = Frame(self, borderwidth=2, relief="ridge",width=self.dwidth/4, height = self.dheight-20)
		self.rightFrame.grid(row=1, column=1, padx=10, pady=2)
		
		
		self.outLabel = Label(self.rightFrame,text="OUTPUT:", bg="gray",justify=LEFT , anchor="w")
		self.outLabel.grid(sticky = W, row=0, column=1)
		
		
		
		self.inLabel = Label(self.rightFrame,text="INPUT:", bg="gray",justify=LEFT , anchor="w")
		self.inLabel.grid(sticky = W, row=15, column=1)
		
		self.text = Text(self.rightFrame, wrap=NONE)
		self.vscrollbar = Scrollbar(self.rightFrame, orient='vert', command=self.text.yview)
		self.text['yscrollcommand'] = self.vscrollbar.set
		self.hscrollbar = Scrollbar(self.rightFrame, orient='hor', command=self.text.xview)
		self.text['xscrollcommand'] = self.hscrollbar.set
		self.text.tag_config("a", foreground="blue", underline=1)
		self.text.tag_config("info", foreground="gray")
		self.text.tag_config("in", foreground="green", background="gray")
		self.text.tag_config("out", foreground="red", background="gray")
		#text.tag_bind("Enter>", show_hand_cursor)
		#text.tag_bind("Leave>", show_arrow_cursor)
		self.text.tag_bind("a","<Button-1>", self.onClick)
		self.text.config(cursor="arrow")
		#self.text.insert(INSERT, "click me!", "a")
		self.text.bind("<Return>", self.OnEnterText)
		self.input = Entry(self.rightFrame, textvariable=self.inline)
		
		
		#self.sportFrame = Frame(self.topFrame, width=100, height = 100)
		#self.sportFrame.grid(row=0, column=0, columnspan=3, padx=10, pady=2)
		
		self.sportLabel = Label(self.topFrame,text="Serial:", bg="gray",justify=LEFT , anchor="w")
		self.sportLabel.grid(sticky = W, row=0, column=0)
		self.serial_port = Entry(self.topFrame, textvariable=self.in_sport)
		self.serial_port.grid(row=0, column=1, pady=3, padx=3, sticky='nsew')
		
		
		self.macLabel = Label(self.topFrame,text="MACAddr:", bg="gray",justify=LEFT , anchor="w")
		self.macLabel.grid(sticky = W, row=0, column=2)
		self.mac = Entry(self.topFrame, textvariable=self.in_mac)
		self.mac.grid(row=0, column=3, pady=3, padx=3, sticky='nsew')		
		
		
		
		self.local_ipLabel = Label(self.topFrame,text="LocalIP:", bg="gray",justify=LEFT , anchor="w")
		self.local_ipLabel.grid(sticky = W, row=0, column=4)
		self.local_ip = Entry(self.topFrame, textvariable=self.in_local_ip)
		self.local_ip.grid(row=0, column=5, pady=3, padx=3, sticky='nsew')
		
		
		self.dest_ipLabel = Label(self.topFrame,text="DestIP:", bg="gray",justify=LEFT , anchor="w")
		self.dest_ipLabel.grid(sticky = W, row=0, column=6)
		self.dest_ip = Entry(self.topFrame, textvariable=self.in_dest_ip)
		self.dest_ip.grid(row=0, column=7, pady=3, padx=3, sticky='nsew')
		
		
		self.gwIpLabel = Label(self.topFrame,text="GwayIP:", bg="gray",justify=LEFT , anchor="w")
		self.gwIpLabel.grid(sticky = W, row=1, column=0)
		self.gwIp = Entry(self.topFrame, textvariable=self.in_gw_ip)
		self.gwIp.grid(row=1, column=1, pady=3, padx=3, sticky='nsew')
		
		
		self.destPortLabel = Label(self.topFrame,text="DestPort:", bg="gray",justify=LEFT , anchor="w")
		self.destPortLabel.grid(sticky = W, row=1, column=2)
		self.dest_port = Entry(self.topFrame, textvariable=self.in_dest_port)
		self.dest_port.grid(row=1, column=3, pady=3, padx=3, sticky='nsew')
		
		
		self.syncPortLabel = Label(self.topFrame,text="SyncPort:", bg="gray",justify=LEFT , anchor="w")
		self.syncPortLabel.grid(sticky = W, row=1, column=4)
		self.sync_port = Entry(self.topFrame, textvariable=self.in_sync_port)
		self.sync_port.grid(row=1, column=5, pady=3, padx=3, sticky='nsew')
		
		
		self.netmaskLabel = Label(self.topFrame,text="NetMask:", bg="gray",justify=LEFT , anchor="w")
		self.netmaskLabel.grid(sticky = W, row=1, column=6)
		self.netmask = Entry(self.topFrame, textvariable=self.in_netmask)
		self.netmask.grid(row=1, column=7, pady=3, padx=3, sticky='nsew')
		
		#-------------------------------------------------------------------
		
		self.ctrlLabel = Label(self.leftFrame,text="CONTROLS:", bg="gray",justify=LEFT , anchor="w")
		self.ctrlLabel.grid(sticky = W, row=0, column=0)
		
		
		#self.engine = Label(self,text="ENGINE",bg="white")
		#self.engine.place(x=10, y=20)
		#self.engine.grid(row=0, column=2)
		self.reBoot = Button(self.leftFrame, text="Reboot Zedboard", width = 15, height = 1,command= self.reboot)
# 		self.reboot.place(x=10, y=200)
		self.reBoot.grid(sticky="nsew", row=btnFcol, column=btnCol)
		
		self.shutdwn = Button(self.leftFrame,text="Shutdown Zedboard", width = 15, height = 1,command= self.shutdown)
# 		self.shutdwn.place(x=10, y=250)
		self.shutdwn.grid(sticky="nsew", row=btnFcol+1, column=btnCol)
		
		self.enginestart = Button(self.leftFrame,text="Audio engine start", width = 15, height = 1,command= self.engineStart)
# 		self.enginestart.place(x=10, y=50)
		self.enginestart.grid(sticky="nsew", row=btnFcol+2, column=btnCol)
		
		self.enginestop = Button(self.leftFrame,text="Audio engine stop", width = 15, height = 1,command= self.engineStop)
# 		self.enginestop.place(x=10, y=100)
		self.enginestop.grid(sticky="nsew", row=btnFcol+3, column=btnCol)
		
# 		self.stream = Label(self.leftFrame,text="STREAM",bg="yellow")
# 		self.stream.place(x=200, y=20)
# 		self.stream.grid(sticky="nsew", row=0, column=btnCol)
		
		self.syncOn = Button(self.leftFrame,text="Sync ON", width = 15, height = 1,command= self.syncon)
# 		self.syncOn.place(x=200, y=50)
		self.syncOn.grid(sticky="nsew", row=btnFcol+4, column=btnCol)
		
		self.syncOff = Button(self.leftFrame,text="Sync OFF", width = 15, height = 1,command= self.syncoff)
# 		self.syncOff.place(x=200, y=100)
		self.syncOff.grid(sticky="nsew", row=btnFcol+5, column=btnCol)
		
		self.strStart = Button(self.leftFrame,text="Start stream", width = 15, height = 1,command= self.strstart)
# 		self.strStart.place(x=200, y=150)
		self.strStart.grid(sticky="nsew", row=btnFcol+6, column=btnCol)
		
		self.strStop = Button(self.leftFrame,text="Stop stream", width = 15, height = 1,command= self.strstop)
# 		self.strStop.place(x=200, y=200)
		self.strStop.grid(sticky="nsew", row=btnFcol+7, column=btnCol)
		
		self.reSet = Button(self.leftFrame,text="Reset", width = 15, height = 1,command= self.reset)
# 		self.reSet.place(x=200, y=250)
		self.reSet.grid(sticky="nsew", row=btnFcol+8, column=btnCol)
		
		self.latncy = Button(self.leftFrame,text="Latency", width = 15, height = 1,command= self.latency)
# 		self.latncy.place(x=650, y=50)
		self.latncy.grid(sticky="nsew", row=btnFcol+9, column=btnCol)
		
# 		self.volumes = Label(self,text="VOLUME",bg="yellow")
# 		self.volumes.place(x=350, y=20)
# 		.grid(sticky="nsew", row=0, column=btnCol)

		self.ctrlFrame = Frame(self, width=self.dwidth-20, height = self.dheight/6, borderwidth=2, relief="ridge")
		self.ctrlFrame.grid(row=2, column=0, columnspan=8, padx=10, pady=2)
		
		self.volDlyLabel = Label(self.ctrlFrame,text="UP/Down Ctrl:", bg="gray",justify=LEFT , anchor="w")
		self.volDlyLabel.grid(sticky = W, row=0, column=0)
		
		self.vol5 = Button(self.ctrlFrame,text="Volume Up", width = 15, height = 1,command= self.volumeUp)
# 		self.vol5.place(x=350, y=50)
		self.vol5.grid(sticky="nsew", row=0, column=1)
 		
		self.vol10 = Button(self.ctrlFrame,text="Volume Down", width = 15, height = 1,command= self.volumeDown)
# 		self.vol10.place(x=350, y=100)
		self.vol10.grid(sticky="nsew", row=0, column=2)
 		
		#self.vol15 = Button(self.leftFrame,text="Volume 15", width = 15, height = 1,command= self.volume15)
# 		self.vol15.place(x=350, y=150)
		#self.vol15.grid(sticky="nsew", row=btnFcol+12, column=btnCol)
 		
# 		self.dlays = Label(self.leftFrame,text="DELAY",bg="yellow")
# # 		self.delays.place(x=500, y=20)
# 		self.dlays.grid(sticky="nsew", row=2, column=0)
 		
 		
		self.dlay5 = Button(self.ctrlFrame,text="Delay Up", width = 15, height = 1,command= self.delayUp)
# 		self.dlay5.place(x=500, y=50)
		self.dlay5.grid(sticky="nsew", row=0, column=3)
 		
		self.dlay10 = Button(self.ctrlFrame,text="Delay Down", width = 15, height = 1,command= self.delayDown)
# 		self.dlay10.place(x=500, y=100)
		self.dlay10.grid(sticky="nsew", row=0, column=4)
 		
		#self.dlay15 = Button(self.leftFrame,text="Delay 15", width = 15, height = 1,command= self.delay15)
# 		self.dlay15.place(x=500, y=150)
		#self.dlay15.grid(sticky="nsew", row=btnFcol+15, column=btnCol)
		
# 		self.measure = Label(self.leftFrame,text="MEASURE",bg="orange")
# 		self.measure.place(x=650, y=20)
# 		.grid(row=0, column=2)


		self.nVolUp = Button(self.ctrlFrame,text="NVolume Up", width = 15, height = 1,command= self.netVolUp)
# 		self.dlay5.place(x=500, y=50)
		self.nVolUp.grid(sticky="nsew", row=0, column=5)
 		
		self.nVolDown = Button(self.ctrlFrame,text="NVolume Down", width = 15, height = 1,command= self.netVolDown)
# 		self.dlay10.place(x=500, y=100)
		self.nVolDown.grid(sticky="nsew", row=0, column=6)


		
		
		self.text.update()
		self.h=int(round(self.winfo_height()/self.text["height"])), int(round(self.winfo_width()/self.text["width"]))
		
		print("OGThe value of H is::",self.h)	
		
		self.bind("<Configure>", self.resize)
		
		#-------------------------------------------------------------------
		# place widgets
		self.text.grid(row=2, column=1,  sticky='nsew')
		self.vscrollbar.grid(row=2, column=2, rowspan=12, sticky='ns')
		self.hscrollbar.grid(row=9, column=1, columnspan=2, sticky='ew')
 		
		self.input.grid(row=17, column=1, columnspan=2, pady=3, padx=3, sticky='ew')
		self.input.bind("<Return>", self.OnEnterInput)
		
		# configuring the wrapper so that the text widget expands
		for i in range(18):
			self.grid_rowconfigure(i, weight=1)
			self.rowconfigure(i, weight=1)
			self.ctrlFrame.grid_rowconfigure(i, weight=1)
			self.text.grid_rowconfigure(i, weight=1)
			self.topFrame.grid_rowconfigure(i, weight=1)
			self.leftFrame.grid_rowconfigure(i, weight=1)
			self.rightFrame.grid_rowconfigure(i, weight=1)
		for n in range(14):
			self.grid_columnconfigure(n, weight=1)
			self.columnconfigure(n, weight=1)
			self.text.grid_columnconfigure(n, weight=1)
			self.ctrlFrame.grid_columnconfigure(n, weight=1)
			self.topFrame.grid_columnconfigure(n, weight=1)
			self.leftFrame.grid_columnconfigure(n, weight=1)
			self.rightFrame.grid_columnconfigure(n, weight=1)
		self.protocol("WM_DELETE_WINDOW", self.onExit)
	def __init__(self):
		Tk.__init__(self)
		self.title(_('Teleporter Test Interface'))
		
		self.dwidth = self.winfo_screenwidth()
		self.dheight =  self.winfo_screenheight()
		print(self.dwidth, self.dheight)
		#Center widget: Half screen dimension - half window dimension
		#self.geometry("350x150+%d+%d" %( ( (self.winfo_screenwidth() / 2.) - (350 / 2.) ), ( (self.winfo_screenheight() / 2.) - (150 / 2.) ) ) )
		self.geometry("%dx%d" %( ( (self.winfo_screenwidth()) ), ( (self.winfo_screenheight())) ) )
# 		self.geometry("800x480")
		self.protocol('WM_DELETE_WINDOW', self.quit) # window close handler
		self.resizable(True, True) # the window can only be resized horizontally
# 		self.container = Frame(self, background="bisque")
# 		self.container.pack(fill="both", expand=True)
		self.createWidgets()

root=Application()
root.mainloop()
sys.exit()
