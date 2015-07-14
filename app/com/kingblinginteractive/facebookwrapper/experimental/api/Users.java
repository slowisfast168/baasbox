/*
 * Copyright (c) 2010-2015 Norbert Bartels.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kingblinginteractive.facebookwrapper.experimental.api;

import com.kingblinginteractive.facebookwrapper.types.Account;
import com.kingblinginteractive.facebookwrapper.types.User;

import java.util.List;

public interface Users {

  /**
   * get user with id <code>userId</code>
   * 
   * @param userId
   * @return
   */
  public User get(String userId);

  /**
   * get the user with id me
   * 
   * @return
   */
  public User getMe();
  
  /**
   * fetch list of account information connected to the user
   * 
   * @return 
   */
  public List<Account> getAccounts();
}
