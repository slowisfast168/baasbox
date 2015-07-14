/*
 * Copyright (c) 2010-2015 Mark Allen.
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

package com.kingblinginteractive.facebookwrapper.types;

import com.kingblinginteractive.facebookwrapper.Facebook;
import com.kingblinginteractive.facebookwrapper.util.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;

public class Payment extends FacebookType {

  /**
   * User who posted the payment.
   * 
   * @return User who posted the payment.
   */
  @Getter @Setter
  @Facebook
  private User user;

  /**
   * actions
   */
  @Getter @Setter
  @Facebook
  private Actions actions;

  @Facebook("created_time")
  private String rawCreatedTime;

  /**
   * items
   */
  @Getter @Setter
  @Facebook
  private Items items;


  @Getter @Setter
  @Facebook
  private String country;

  /**
   * payment created time
   */
  @Getter @Setter
  @Facebook("created_time")
  private String createdTime;

  /**
   * exchagne rate
   */
  @Getter @Setter
  @Facebook("payout_foreign_exchange_rate")
  private double payoutForeignExchangeRate;

  private static final long serialVersionUID = 2L;

  /**
   * Contains the User of the payment data
   *
   */
  public static class User extends FacebookType {

    @Getter
    @Setter
    @Facebook
    private String name;

    @Getter
    @Setter
    @Facebook
    private String id;

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
      return ReflectionUtils.hashCode(this);
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object that) {
      return ReflectionUtils.equals(this, that);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
      return ReflectionUtils.toString(this);
    }
  }

  /**
   * Contains the action of the payment data
   * 
   */
  public static class Actions extends FacebookType {

      public static class  ActionDetail extends FacebookType {
        @Getter
        @Setter
        @Facebook
        private String type;

        @Getter
        @Setter
        @Facebook
        private String status;

        @Getter @Setter
        @Facebook
        private String currency;

        @Getter @Setter
        @Facebook
        private String amount;

        @Getter @Setter
        @Facebook("time_created")
        private String timeCreated;

        @Getter @Setter
        @Facebook("time_updated")
        private String timeUpdated;

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
          return ReflectionUtils.hashCode(this);
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object that) {
          return ReflectionUtils.equals(this, that);
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
          return ReflectionUtils.toString(this);
        }
      }

    /**
     * actions
     */
    @Getter @Setter
    //@Facebook
    private ActionDetail[] actionDetails;

    }


  /**
   * Contains the itme of the payment data
   *
   */
  public static class Items extends FacebookType {

    public  static class  ItemDetail extends FacebookType {
      @Getter
      @Setter
      @Facebook
      private String type;

      @Getter
      @Setter
      @Facebook
      private String product;

      @Getter @Setter
      @Facebook
      private int quantity;

      /**
       * @see Object#hashCode()
       */
      @Override
      public int hashCode() {
        return ReflectionUtils.hashCode(this);
      }

      /**
       * @see Object#equals(Object)
       */
      @Override
      public boolean equals(Object that) {
        return ReflectionUtils.equals(this, that);
      }

      /**
       * @see Object#toString()
       */
      @Override
      public String toString() {
        return ReflectionUtils.toString(this);
      }
    }

    @Getter
    @Setter
    //@Facebook
    private ItemDetail[] itemDetails;
  }
}