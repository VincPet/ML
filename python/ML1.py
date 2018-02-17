
# coding: utf-8

# In[4]:


import numpy as np
import os.path
path="C:/Users/Universita/"

if not os.path.isfile(path+"btc_data.npy"):
    btc_data = np.loadtxt(path+'btc_data.csv', delimiter=',', 
                    converters = {2: lambda x: 1 if x==b't' else -1},
                     dtype={'names': ('date','average', 'up'),
                            'formats': ('datetime64[D]', 'f4', 'i4')})
    np.save(path+'btc_data.npy', btc_data);
else:
    btc_data = np.load(path+'btc_data.npy');

if not os.path.isfile(path+"training.npy"):
    
    addresses_date =  np.loadtxt(path+'training.csv', delimiter=',', 
                     dtype={'names': ('id', 'date', 'times'),
                            'formats': ('i4', 'datetime64[D]', 'f8')});
    
    np.save(path+'training.npy', addresses_date);
else:
    addresses_date = np.load(path+'training.npy');
       


# In[5]:


from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers import Conv2D,LeakyReLU, Dropout, Flatten
from keras.optimizers import RMSprop, Adam

def get_discr(filename):
    # if (os.path.isfile(filename)):
    #  return load_model(filename)

    D = Sequential()
    depth = 64
    dropout = 0.25
    # In: 28 x 28 x 1, depth = 1
    # Out: 14 x 14 x 1, depth=64
    input_shape = (128, 128, 1)
    D.add(Conv2D(depth * 1, 5, strides=2, input_shape=input_shape))
    D.add(LeakyReLU(alpha=0.2))
    D.add(Dropout(dropout))
    D.add(Conv2D(depth * 2, 5, strides=2, padding='same'))
    D.add(LeakyReLU(alpha=0.2))

    D.add(Dropout(dropout))
    D.add(Conv2D(depth * 4, 5, strides=2, padding='same'))
    D.add(LeakyReLU(alpha=0.2))

    D.add(Dropout(dropout))
    D.add(Conv2D(depth * 8, 5, strides=1, padding='same'))
    D.add(LeakyReLU(alpha=0.2))

    D.add(Dropout(dropout))
    # Out: 1-dim probability
    D.add(Flatten())
    D.add(Dense(1))
    D.add(Activation('sigmoid'))
    D.summary()
    return D


def get_discr_NN(filename):
    # if (os.path.isfile(filename)):
    #  return load_model(filename)

    D = Sequential()
    depth = 64
    dropout = 0.25
    # In: 28 x 28 x 1, depth = 1
    # Out: 14 x 14 x 1, depth=64

    # Out: 1-dim probability
    D.add(Dense(4096, input_shape = (128, 128, 1)))
    D.add(LeakyReLU(alpha=0.2))
    D.add(Dense(1024))
    D.add(LeakyReLU(alpha=0.2))
    D.add(Dense(128))
    D.add(LeakyReLU(alpha=0.2))
    D.add(Flatten())
    D.add(Dense(1))
    D.add(Activation('sigmoid'))
    D.summary()
    return D


# In[7]:


from itertools import groupby
from operator import itemgetter
from collections import defaultdict


y = {}

for date,avg,up in btc_data:
    y[date]=up;
    
x = {}


for id,date,times in addresses_date:
    l=x.setdefault(date, np.zeros(128*128, dtype=float))
    l[id-1] = times;

num=len(x);
x_int = np.empty([num,128,128,1]);
y_int= np.zeros(num);


num=0;

for k,v in x.items():
    x_int[num] = v.reshape([128,128,1])
    y_int[num]= 1 if y[k]==1 else 0;
    num=num+1

    


# In[8]:


def get_x_y(lenght):
    tmp=np.random.randint(0, x_int.shape[0], size=lenght)
    snap = x_int[tmp]
    res=y_int[tmp]
    return (snap, res)


# In[13]:


D = get_discr_NN("D")
opt_dm = Adam(lr=0.001)
D.compile(loss='binary_crossentropy', optimizer=opt_dm, metrics=['accuracy'])

def train(times, batch_lenght):
    for i in range(times):
        x, y = get_x_y(batch_lenght)
        d_loss = D.train_on_batch(x, y)
        
        if (i % 10 == 0):
            print("Step \t {0} \t d_loss: \t {1}".format(i, d_loss))
        
train(10000, 6)

