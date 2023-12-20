/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.shared.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.shared.logic.Constraint;

/**
 * Base class for validator implementations.
 * 
 * Encapsulates the notion of an identifier for each validator class, and helper
 * methods for constructing status metadata.
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class BaseValidator extends AbstractIdentifiableInitializableComponent {

    /**
     * Message format string.
     *
     * The generated message is formatted using this with the object being validated passed
     * as an argument.
     *
     * Defaults to <code>"value rejected: '%s'"</code>.
     *
     * @since 0.10.0
     */
    @Nonnull @GuardedBy("this")
    private String message = "value rejected: '%s'";

    /**
     * Returns the message format string.
     *
     * @return the message format string
     *
     * @since 0.10.0
     */
    @Nonnull
    public final synchronized String getMessage() {
        return message;
    }

    /**
     * Set the message format string.
     * 
     * @param newMessage the new message format string
     *
     * @since 0.10.0
     */
    public final synchronized void setMessage(@Nonnull final String newMessage) {
        checkSetterPreconditions();
        message = Constraint.isNotNull(newMessage, "message format string may not be null");
    }

    /**
     * Construct a modified component identifier from the stage identifier and the
     * validator identifier.
     * 
     * @param stageId identifier for the calling stage
     * 
     * @return composite component identifier
     */
    private @Nonnull String makeComponentId(@Nonnull final String stageId) {
        final String id = getId();
        if (id == null) {
            return stageId;
        }
        return stageId + "/" + getId();
    }

    /**
     * Add an {@link ErrorStatus} to the given {@link Item}.
     * 
     * @param mess message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addError(@Nonnull final String mess, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        item.getItemMetadata().put(new ErrorStatus(makeComponentId(stageId), mess));
    }
    
    /**
     * Add a {@link WarningStatus} to the given {@link Item}.
     * 
     * @param mess message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addWarning(@Nonnull final String mess, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        item.getItemMetadata().put(new WarningStatus(makeComponentId(stageId), mess));
    }
    
    /**
     * Add a {@link WarningStatus} or {@link ErrorStatus} to the given {@link Item}.
     * 
     * @param error <code>true</code> if an {@link ErrorStatus} should be added
     * @param mess message to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     */
    protected void addStatus(final boolean error, @Nonnull final String mess, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        if (error) {
            addError(mess, item, stageId);
        } else {
            addWarning(mess, item, stageId);
        }
    }

    /**
     * Formats the message with the given subject.
     *
     * @param subject subject of the validation, to be provided as an argument to the
     *      formatting string.
     * @return formatted message
     */
    protected @Nonnull String formatMessage(final @Nonnull Object subject) {
        final String mess = String.format(getMessage(), subject);
        assert mess != null;
        return mess;
    }

    /**
     * Formats the message with the given subject.
     *
     * <p>
     * If the supplied {@link Throwable} has a non-<code>null</code> detail
     * message, this is added to the resulting formatted string.
     * </p>
     *
     * @param subject subject of the validation, to be provided as an argument to the
     *      formatting string.
     * @param cause a {@link Throwable} which is to be interpreted as the cause of the
     *      validation failure.
     * @return formatted message
     */
    protected @Nonnull String formatMessage(final @Nonnull Object subject, final @Nonnull Throwable cause) {
        final @Nullable var cmsg = cause.getMessage();
        if (cmsg == null) {
            return formatMessage(subject);
        } else {
            return formatMessage(subject) + " (" + cmsg + ")";
        }
    }
    
    /**
     * Add an {@link ErrorStatus} to the given {@link Item}.
     *
     * <p>
     * The status message included in the {@link ErrorStatus} is generated
     * by formatting the provided value with the {@link #message} field.
     * </p>
     *
     * @param extra extra value to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     *
     * @since 0.10.0
     */
    protected void addErrorMessage(@Nonnull final Object extra, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        addError(formatMessage(extra), item, stageId);
    }

    /**
     * Add an {@link ErrorStatus} to the given {@link Item}.
     *
     * <p>
     * The status message included in the {@link ErrorStatus} is generated
     * by formatting the provided value with the {@link #message} field.
     * </p>
     *
     * @param extra extra value to include in the status metadata
     * @param item {@link Item} to add the status metadata to
     * @param stageId component identifier for the calling stage
     * @param cause reason for the validation failure, as a {@link Throwable}
     *
     * @since 0.10.0
     */
    protected void addErrorMessage(@Nonnull final Object extra, @Nonnull final Item<?> item,
            @Nonnull final String stageId, @Nonnull final Throwable cause) {
        addError(formatMessage(extra, cause), item, stageId);
    }
}
