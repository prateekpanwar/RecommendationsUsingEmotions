# -*- coding: utf-8 -*-
"""
Created on Thu Jan 18 15:50:07 2018

@author: 100605231
"""
import time 
import numpy as np
import pandas as pd
from scipy.signal import butter, lfilter, find_peaks_cwt
import matplotlib.pylab as mat
from collections import Counter
import threading
import peakutils
import pickle
import csv
import sys
import socket

UDP_IP = "127.0.0.1"
UDP_PORT = 5005

gsr_list = [];
pupil_list = []
final_time = 0
main_list = []
temp = []
new_time = []
gaze_valid = []
gaze_x = []
gaze_y = []
X = []
Y = []
SX = []
SY = []

temp_mean=[]
temp_mean2 = []
def gaze_token(pupil, gazeX, gazeY, valid):
    global pupil_list, new_time, gaze_valid, gaze_x, gaze_y
    new_time.append(final_time)
    
    if len(Counter(new_time)) == 1:
        pupil_list.append(pupil)
        gaze_x.append(gazeX)
        gaze_y.append(gazeY)
        gaze_valid.append(valid)
       # print(final_time)
    else:
        pupil_list.append(pupil)
        gaze_x.append(gazeX)
        gaze_y.append(gazeY)
        gaze_valid.append(valid)
        #print("Gaze", pupil_list)
        new_time=[]
        new_time.append(final_time)
        val = int(len(pupil_list) - (len(pupil_list) * 0.6))
        print("**************************************************",val,'***********', len(pupil_list), pupil, gazeX, gazeY)
        del pupil_list[0:val]
        del gaze_valid[0:val]
        del gaze_x[0:val]
        del gaze_y[0:val]

def token(data, time):
    global final_time, gsr_list    
    if len(gsr_list) < 49:
        gsr_list.append(data)
    else:
        gsr_list.append(data)
        #print("GSR", gsr_list)
        final_time = time
       # print(time)
        sendData_gaze()
        sendData(gsr_list)
        del gsr_list[0:20]
        
def sendData_gaze():
    global mean_fixation, mean_pd, std_pd, no_peaks_pd    
    neg_index=[]
    pos_index = []
    final_signal = []
    int_gaze_x = []
    int_gaze_y = []
    count = 0
    for x in gaze_valid:
        if x >= 2:
            neg_index.append(count)
        else:
            pos_index.append(count)
            final_signal.append(pupil_list[count])
            int_gaze_x.append(gaze_x[count])
            int_gaze_y.append(gaze_y[count])
        count+=1
        
    #################################PD################################
    try:
        interpolated_values = np.interp(neg_index, pos_index, final_signal)
        list2 = np.arange(0.0,float(len(pupil_list)))
        list2[pos_index] = final_signal
        list2[neg_index] = interpolated_values
        
        interpolated_values = np.interp(neg_index, pos_index, int_gaze_x)
        list3 = np.arange(0.0, float(len(gaze_x)))
        list3[pos_index] = int_gaze_x
        list3[neg_index] = interpolated_values
        
        interpolated_values = np.interp(neg_index, pos_index, int_gaze_y)
        list4 = np.arange(0.0, float(len(gaze_y)))
        list4[pos_index] = int_gaze_y
        list4[neg_index] = interpolated_values          
    #####################################################################
        filterData_gaze(list2, list3, list4)
    except:
        mean_fixation = None
        mean_pd = None 
        std_pd =None 
        no_peaks_pd = None
        print("Look at the screen")
    
    
def filterData_gaze(pupil, gazeX, gazeY):
    global mean_pd, std_pd, no_peaks_pd
    b,a = butter(1, 0.9999)
    pupil = lfilter(b,a, pupil)
    gazeX = lfilter(b,a, gazeX)
    gazeY = lfilter(b,a, gazeY)
    
    fixation_saccade(gazeX, gazeY)
    ##################################zscore#############################
    mean = np.mean(pupil)
    std = np.std(pupil)
    new_list = (pupil-mean)/std
    #mat.plot(new_list)
    #mat.show()
    #################################lang################################
    c=[]
    for i in range(3, len(new_list)-3):
       c.append( 2000*((2*new_list[3+i])+new_list[2+i]-(2*new_list[i+1])-(2*new_list[i])-(2*new_list[i-1])+new_list[i-2]+(2*new_list[i-3]))/((60*60)*20) )
    #mat.plot(c)
    #mat.show()
    mean_pd = np.mean(c)
    std_pd = np.std(c)
    no_peaks_pd = len(peakutils.peak.indexes(c, thres=np.std(c)))
    print('----------------------------------------------------------------------------')
    

def fixation_saccade(gazeX, gazeY):
    global mean_fixation, centroidX, centroidY
    r_window = 5;
    d = [];
    
    for ii in range(4, len(gazeX)-4):
        s_x_b = gazeX[ii-r_window+1:ii];
        s_y_b = gazeY[ii-r_window+1:ii];
        mean_before = np.array([np.mean(s_x_b), np.mean(s_y_b)]);
        s_x_a = gazeX[ii: ii+r_window-1];
        s_y_a = gazeY[ii: ii+r_window-1];
        mean_after = np.array([np.mean(s_x_a), np.mean(s_y_a)]);      
        d_temp = np.sqrt(np.dot((mean_after-mean_before), np.transpose(mean_after-mean_before)));
        d.append(d_temp);
    
    peak_index = (peakutils.peak.indexes(d, thres=np.std(d))+4).tolist()
    #print(len(peak_index)) 
    peak_index.insert(0,1)
    peak_index.append(len(gazeX))
    len_index = len(peak_index)
    
    fixation_duration = np.diff(peak_index)
    
    X.append(gazeX)
    Y.append(gazeY)
    saccade_x_points = []
    saccade_y_points = []
    for jj in range(0, len_index-1):
        saccade_x_points.append(np.median(gazeX[peak_index[jj]:peak_index[jj+1]]))
        saccade_y_points.append(np.median(gazeY[peak_index[jj]:peak_index[jj+1]]))
                
    SX.append(saccade_x_points)
    SY.append(saccade_y_points)
    
    centroidX = np.mean(saccade_x_points)
    centroidY = np.mean(saccade_y_points)
    
    mean_fixation = np.mean(fixation_duration)

def sendData(GSR_list):
    #print("Gaze", pupil_list, gaze_valid)
    neg_index=[]
    pos_index = []
    final_signal = []
    count = 0
    for x in GSR_list:
        if x < 0:
            neg_index.append(count)
        else:
            pos_index.append(count)
            final_signal.append(x)
        count+=1
    interpolated_values = np.interp(neg_index, pos_index, final_signal)
    list2 = np.arange(0.0,float(len(GSR_list)))
    list2[pos_index] = final_signal
    list2[neg_index] = interpolated_values
    filterData(list2)
    
def filterData(GSR_list): 
    GSR_list = 1e6/GSR_list
    temp.append(GSR_list)
    b,a = butter(1, 0.9999)
    GSR_list = lfilter(b,a,GSR_list)  
    main_list.append(GSR_list)
    zscore(main_list, GSR_list)

def zscore(full_list, GSR_list):
    global std_gsr_zscore
    mean = np.mean(full_list)
    std = np.std(full_list)
    new_list = (GSR_list-mean)/std
    #mat.plot(new_list)
    #mat.show()
    #mat.plot(gsr_list)
    std_gsr_zscore = np.std(new_list)
    lang(new_list)

window_count = 0
def lang(new_list):
    global std_gsr_lang, mean_gsr, window_count    
    c=[]
    for i in range(3, len(new_list)-3):
       c.append( 2000*((2*new_list[3+i])+new_list[2+i]-(2*new_list[i+1])-(2*new_list[i])-(2*new_list[i-1])+new_list[i-2]+(2*new_list[i-3]))/((5*5)*20) )
    
    #mat.plot(c)
    #mat.show()
    std_gsr_lang = np.std(c)
    mean_gsr = np.mean(c)
    #print(std_gsr_lang, int(mean_fixation), mean_pd, std_pd, std_gsr_zscore, no_peaks_pd, mean_gsr)+
    if (mean_pd != None):
        try:
            pred()
        except:
            print(window_count,'$$$$$$$$$thing is still going')
    else:
        print('might be done', window_count)
    
    window_count+=1
   
std_gsr_lang = 0
std_gsr_zscore = 0
mean_gsr = 0
mean_pd = 0
no_peaks_pd = 0
std_pd = 0
mean_fixation = 0
centroidX = 0
centroidY = 0

f = pd.read_csv('13GSR.csv')
x = f.iloc[:, 2]
f = f.iloc[:,0]

p = pd.read_csv('13eye.csv')
valid_p = p.iloc[:, 4]
p_pd = p.iloc[:, 1]
p_x = p.iloc[:, 2]
p_y = p.iloc[:, 3]


#data=1
#while(data<100):
#    token(data,data,5)
#    gaze_token()
#    data+=1

#for data in range(len(f)):
#    token(f[data], x[data], 5)
def fast():
    for i in range(1,len(p_x)):
        time.sleep(.01666)
        gaze_token(p_pd[i], p_x[i], p_y[i], valid_p[i])
       # print('*************************************', i)
       
def slow():
    for i in range(1,len(f)):
        time.sleep(0.2)
        token(f[i], x[i])

#fast()
#slow()
clf = pickle.load( open( "13", "rb" ) )

threading.Thread(target=slow).start()
threading.Thread(target=fast).start()

final=[]
sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
def pred():
    panel = ""                                # 'r', 'k', 'e', 'f', 'n', 'd', q
    example_measures = np.array([(std_gsr_lang, mean_fixation, mean_pd, std_pd, std_gsr_zscore, no_peaks_pd, mean_gsr)])
    #example_measures = np.array([(0,0,0,0,0,0,0)])
    example_measures = example_measures.reshape(len(example_measures), -1);
    prediction = clf.predict(example_measures)
    print(prediction)
    
    print(centroidX, centroidY, end='')
    if  500/1920 > (centroidX) and (centroidY) < 275/1080:
        print('Search Panel')
        panel = 'a'
    elif 500/1920 <= (centroidX) and (centroidY) < 275/1080:
        print('Task Panel')
        panel = 'b'
    elif 1400/1920 <= (centroidX) and (centroidY) >= 275/1080:
        if (centroidY) <= 740/1080:
            print('Information panel')
            panel = 'c'
        else:
            print('Help Panel')
            panel = 'd'
    elif 1400/1920 > (centroidX) and (centroidY) >= 780/1080:
        print("filterX")
        panel = 'e'
    else:
        print("Dataset")
        panel = 'f'
        
    
    sock.sendto(str(prediction+panel).encode(), (UDP_IP, UDP_PORT))
    #final.append(np.array([(std_gsr_lang, mean_fixation, mean_pd, std_pd, std_gsr_zscore, no_peaks_pd, mean_gsr)]))
    
