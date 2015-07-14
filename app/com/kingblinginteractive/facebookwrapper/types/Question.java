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
import com.kingblinginteractive.facebookwrapper.JsonMapper.JsonMappingCompleted;
import com.kingblinginteractive.facebookwrapper.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Represents the <a href="http://developers.facebook.com/docs/reference/api/question">Question Graph API type</a>.
 * 
 * @author <a href="http://restfb.com">Mark Allen</a>
 * @since 1.6.10
 */
public class Question extends FacebookType {

  /**
   * User who asked the question.
   * 
   * @return User who asked the question.
   */
  @Getter
  @Setter
  @Facebook
  private NamedFacebookType from;

  /**
   * Text of the question.
   * 
   * @return Text of the question.
   */
  @Getter
  @Setter
  @Facebook
  private String question;

  @Facebook("created_time")
  private String rawCreatedTime;

  @Facebook("updated_time")
  private String rawUpdatedTime;

  /**
   * Time when question was created.
   * 
   * @return Time when question was created.
   */
  @Getter
  @Setter
  private Date createdTime;

  /**
   * Time when question was last updated.
   * 
   * @return Time when question was last updated.
   */
  @Getter
  @Setter
  private Date updatedTime;

  @Facebook
  private List<QuestionOption> options = new ArrayList<QuestionOption>();

  private static final long serialVersionUID = 1L;

  /**
   * The list of options available as answers to the question.
   * 
   * @return The list of options available as answers to the question.
   */
  public List<QuestionOption> getOptions() {
    return unmodifiableList(options);
  }

  public boolean addOption(QuestionOption option) {
    return options.add(option);
  }

  public boolean removeOption(QuestionOption option) {
    return options.remove(option);
  }

  @JsonMappingCompleted
  void convertTime() {
    createdTime = DateUtils.toDateFromLongFormat(rawCreatedTime);
    updatedTime = DateUtils.toDateFromLongFormat(rawUpdatedTime);
  }
}