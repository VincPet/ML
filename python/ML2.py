
# coding: utf-8

# In[93]:


import numpy as np
import os.path
path="C:/Users/Universita/"

#load and parse the file
file_name="btc_value_min"
if not os.path.isfile(path+file_name+".npy"):
    btc_data = np.loadtxt(path+file_name+'.csv', delimiter=',', 
                     dtype={'names': ('time', 'close_p', 'perc'),
                            'formats': ('datetime64[h]','f4', 'f8')})
    np.save(path+file_name+'.npy', btc_data);
else:
    btc_data = np.load(path+file_name+'.npy');
    


# In[94]:


#Will contain percentages
x=np.empty((btc_data.shape[0],1));
#Will contain btc value
val=np.empty(btc_data.shape[0]);
#Will contain dates
dat={}

num=0;

for time, value, percentage in btc_data:
    x[num]=percentage;
    val[num]=value;
    dat[num]=time;
    num+=1;
        
        


# In[95]:


window_size=300;
prediction_size=15;


def get_x_y(lenght, bottom, upper):
    #random <lenght> start index for the random vector to create
    indexs=np.random.randint(bottom, min(upper, x.shape[0]-window_size-prediction_size), size=lenght)
    #empty vector of size [num_vectors=lenght], [subsequent items in vector=rep], 1
    windows=np.empty((lenght,window_size,1));
    #array containing percentages relative to the end of the windows_size
    expected=np.empty(lenght);
    num=0;
    for i in indexs:
        #array from i to i+window_size
        windows[num] = x[i:i+window_size]
        
        #expected result is 1 if value is going up in next 15 clock, else 0
        expected[num]=np.sign(val[i+window_size+prediction_size]-val[i+window_size]);
        expected[num]=1 if expected[num]==1 else 0; 
        num+=1;
        
    return (windows, expected)

reserved_for_test=70000;
number_of_tests=64;

test_x, test_y=get_x_y(number_of_tests, x.shape[0]-reserved_for_test, x.shape[0]);


# In[96]:


from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import Conv1D, Dropout, Flatten
from keras.optimizers import RMSprop, Adam

def get_discr(filename):
    # if (os.path.isfile(filename)):
    #  return load_model(filename)

    D = Sequential()
    depth = 32
    dropout = 0.25

    input_shape = (rep, 1)
    D.add(Conv1D(depth * 1, 15, strides=2, input_shape=input_shape))
    D.add(Activation('relu'))
    D.add(Dropout(dropout))
    D.add(Conv1D(depth * 2, 10, strides=2, padding='same'))
    D.add(Activation('relu'))

    D.add(Dropout(dropout))
    D.add(Conv1D(depth * 4, 5, strides=2, padding='same'))
    D.add(Activation('relu'))

    D.add(Dropout(dropout))
    D.add(Flatten())
    D.add(Dense(64))
    D.add(Activation('relu'))
    D.add(Dropout(dropout))
    # Out: 1-dim probability
    D.add(Dense(1))
    D.add(Activation('sigmoid'))
    D.summary()
    return D


# In[97]:


D = get_discr("D")
opt_dm = Adam(lr=0.001)
D.compile(loss='binary_crossentropy', optimizer=opt_dm, metrics=['accuracy'])


def train(times, batch_lenght):
    for i in range(times):
        a, b = get_x_y(batch_lenght, 0, x.shape[0]-reserved_for_test-window_size)
        d_loss = D.train_on_batch(a, b)
        
        if (i % 10 == 0):
            print("Step \t {0} \t d_loss: \t {1}".format(i, d_loss))
            # evaluate the model
            scores = D.evaluate(test_x, test_y, verbose=0)
            print("\n%s: %.2f%%" % (D.metrics_names[1], scores[1]*100))

train(50000, 1024)

